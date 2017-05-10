package com.krizalys.database_testing

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

trait CowResource {
}

class DatabaseEntity extends CowResource {
}

class Big extends CowResource {
    var referenceCount = 0
    var value = "default"

    def duplicate(): Big = {
        val clone = new Big
        clone.referenceCount = 1
        clone.value = value
        clone
    }

    def use(): Unit = {
        referenceCount = referenceCount + 1
    }
}

object Global {
    def copyBig(original: Big): Big = {
        original.duplicate
    }
}

class Complex(var big: Big) {
    big.use

    def setValue(value: String, cow: Big => Big): Unit = {
        if (big.referenceCount > 1) {
            big.referenceCount = big.referenceCount - 1
            big = cow(big)
        }

        big.value = value
    }

    def printValue(): Unit = {
        println(big.value)
    }
}

object Sample {
    case class Period(id: Int = 0) {}
    case class GroupRateType(id: Int = 0, period: Option[Period] = None) {}
    case class RoomRateCategoryPackage(id: Int = 0, groupRateType: Option[GroupRateType] = None) {}

    def saveRatesByRoomTypeId(groupRateType: GroupRateType, roomTypeId: Int) = {
        val groupRateTypeCounts = Map(
            1 -> 2,
            3 -> 1
        )

        val rrcp = Set(
            RoomRateCategoryPackage(
                id = 1,
                groupRateType = Some(GroupRateType(
                    id = 1,
                    period = Some(Period(id = 1))
                ))
            ),
            RoomRateCategoryPackage(
                id = 2,
                groupRateType = Some(GroupRateType(
                    id = 1, // Shared with previous
                    period = Some(Period(id = 1))
                ))
            ),
            RoomRateCategoryPackage(id = 3, groupRateType = Some(GroupRateType(id = 3)))
        )
    }
}

/*trait CowResource {
    val referenceCount = 1

    def copy(): CowResource
}

trait CowResourceHolder {
    val cowResource: CowResource

    def write(data: CowResource): Unit = {
        val referenceCount = cowResource.referenceCount - 1

        if (referenceCount <= 0) {
            // delete
        } else {
            // Resource was shared more than once, must be copied.
            //cowResource = cowResource.copy()
        }
    }
}*/

class CowTest extends FlatSpec with MockFactory {
    it should "work" in {
        val big = new Big
        val complex1 = new Complex(big)
        val complex2 = new Complex(big)
        complex1.setValue("changed", Global.copyBig)
        complex1.printValue
        complex2.printValue
        println(complex1.big.referenceCount)
        println(complex2.big.referenceCount)
    }

    it should "work #2" in {
        val big = new Big
        val sut = new Complex(big)
        val other = new Complex(big)
        val cow = mockFunction[Big, Big]
        cow expects big returning big once()
        sut.setValue("test", cow)
    }
}
