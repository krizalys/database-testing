package com.krizalys.database_testing

import net.jcazevedo.moultingyaml._
import net.jcazevedo.moultingyaml.DefaultYamlProtocol._

import scala.io.Source

object InitialOperation {
    def apply(operation: Operation): Option[String] = operation match {
        case operation: IdentityOperation => Some(operation.getValue)
        case operation: DeletionOperation => Some(operation.getValue)
        case _ => None
    }
}

object FinalOperation {
    def apply(operation: Operation): Option[String] = operation match {
        case operation: IdentityOperation => Some(operation.getValue)
        case operation: AdditionOperation => Some(operation.getValue)
        case _ => None
    }
}

object Types {
    type Row = scala.collection.immutable.HashMap[String, String]
}

case class RowFormatContext(var iRow: Int = 0) {
}

class RowFormat(table: Table) extends net.jcazevedo.moultingyaml.YamlFormat[Types.Row] {
    def write(value: Types.Row) = {
        YamlArray()
    }

    def read(value: YamlValue): Types.Row = {
        implicit val context = new RowFormatContext()

        value match {
            case value: YamlArray => {
                value
                    .elements
                    .map(stringify(_))
                    .foldLeft(new Types.Row()) {
                        (lhs: Types.Row, rhs: String) => {
                            lhs + (getColumnName -> rhs)
                        }
                    }
            }

            case value: YamlObject => {
                value
                    .fields
                    .foldLeft(new Types.Row()) {
                        (lhs: Types.Row, tuple: (YamlValue, YamlValue)) => {
                            lhs + (stringify(tuple._1) -> stringify(tuple._2))
                        }
                    }
            }

            case _ => deserializationError("Array expected")
        }
    }

    def getColumnName(implicit context: RowFormatContext): String = {
        val name = table.columnsByIndex(context.iRow).name
        context.iRow = context.iRow + 1
        name
    }

    /** Output quoted SQL value (if string), or unquoted value (if number), etc... */
    def stringify(element: YamlValue): String = element match {
        case value: YamlNumber => value.convertTo[Int].toString
        case value: YamlString => s"${value.convertTo[String]}"
        case _ => deserializationError("Number or string expected")
    }
}

/*object MyYamlProtocol extends DefaultYamlProtocol {
    implicit val rowFormat = yamlFormat4(Color)
}*/

class InsertQuery(table: Table, value: net.jcazevedo.moultingyaml.YamlValue) {
    override def toString = {
        implicit val rowFormat = new RowFormat(table)

        val converted = value match {
            case value: net.jcazevedo.moultingyaml.YamlArray => value.convertTo[Vector[Types.Row]]
            case _ =>
        }

        println(converted)
        val columns = table.columnsByIndex.map(_.name).reduceLeft(_ + ", " + _)
        s"INSERT INTO ${table.name}(${columns}) VALUES()"
    }

    /*private def convertValue(value: net.jcazevedo.moultingyaml.YamlValue) = value match {
        case value: net.jcazevedo.moultingyaml.YamlNumber => value.convertTo[Long]
        case value: net.jcazevedo.moultingyaml.YamlString => value.convertTo[String]
    }

    private def convertRow(value: net.jcazevedo.moultingyaml.YamlArray) = {
        implicit val rowFormat = new RowFormat()

        value.convertTo[Vector[Row]]/*.map {
            value => convertValue(value)
        }*/
    }*/
}

object InitialQueryProducer {
    def apply(table: Table, operation: Operation): Option[InsertQuery] = operation match {
        case operation: IdentityOperation => Some(new InsertQuery(table, operation.toString.parseYaml))
        case operation: DeletionOperation => Some(new InsertQuery(table, operation.toString.parseYaml))
        case _ => None
    }
}

object Program {
    /**
     * @param arguments
     *        The arguments.
     */
    def main(arguments: Array[String]): Unit = {
        val table = Table(name = "test_table")
        table.addColumn(Column(name = "id"))
        table.addColumn(Column(name = "first_name"))
        table.addColumn(Column(name = "last_name"))

        var initialRows: Seq[Option[String]] = Nil
        var finalRows: Seq[Option[String]] = Nil

        Source
            .fromFile("example/data.yml.diff")
            .getLines
            .map(Operation(_))
            .foreach {
                operation => {
                    initialRows = initialRows :+ InitialOperation(operation)
                    finalRows = finalRows :+ FinalOperation(operation)
                }
            }

        print(initialRows)
        print(finalRows)
    }

    def print(values: Seq[Option[String]]): Unit = {
        val v = values
            .flatMap(value => value)
            .map(_ + "\n")
            .reduce(_ + _)

        println(v)
        println(v.parseYaml)
    }
}
