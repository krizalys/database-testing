package com.krizalys.database_testing

/**
 * @param name
 *        The name.
 * @param columnsByName
 *        The columns, by name.
 * @param columnsByIndex
 *        The columns, by index.
 */
case class Table(
    name: String,
    var columnsByName: Map[String, Column] = Map(),
    var columnsByIndex: Seq[Column] = Nil
) {
    /**
     * @param column
     *        The column.
     */
    def addColumn(column: Column): Unit = {
        columnsByName = columnsByName + (column.name -> column)
        columnsByIndex = columnsByIndex :+ column
    }
}
