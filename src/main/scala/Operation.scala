package com.krizalys.database_testing

trait Operation {
    override def toString(): String = getValue

    def getValue(): String
}

class IdentityOperation(value: String) extends Operation {
    override def getValue() = value
}

class AdditionOperation(value: String) extends Operation {
    override def getValue() = value
}

class DeletionOperation(value: String) extends Operation {
    override def getValue() = value
}

object Operation {
    /**
     * @param line
     *        The line.
     *
     * @returns The operation.
     */
    def apply(line: String): Operation = {
        val value = line.substring(1)

        line(0) match {
            case ' ' => new IdentityOperation(value)
            case '+' => new AdditionOperation(value)
            case '-' => new DeletionOperation(value)
        }
    }
}
