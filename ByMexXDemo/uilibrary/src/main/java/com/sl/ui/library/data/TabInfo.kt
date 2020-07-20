package com.sl.ui.library.data

import android.os.Parcel
import android.os.Parcelable

class TabInfo() : Parcelable{
    var leftIcon = 0
    var name: String?=""
    var index = 0
    var extras: Any? = null
    var selected = false
    var netIcon = ""

    constructor(parcel: Parcel) : this() {
        leftIcon = parcel.readInt()
        name = parcel.readString()
        index = parcel.readInt()
        selected = parcel.readByte() != 0.toByte()
        netIcon = parcel.readString()?:""
    }


    constructor(name: String, index: Int) : this() {
        this.name = name
        this.index = index
    }

    constructor(name: String, index: Int,selected:Boolean) : this() {
        this.name = name
        this.index = index
        this.selected = selected
    }


    constructor(name: String, index: Int,leftIcon:Int) : this() {
        this.name = name
        this.index = index
        this.leftIcon = leftIcon
    }

    constructor(name: String, extras: Any?) : this() {
        this.name = name
        this.extras = extras
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(leftIcon)
        parcel.writeString(name)
        parcel.writeInt(index)
        parcel.writeByte(if (selected) 1 else 0)
        parcel.writeString(netIcon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TabInfo> {
        override fun createFromParcel(parcel: Parcel): TabInfo {
            return TabInfo(parcel)
        }

        override fun newArray(size: Int): Array<TabInfo?> {
            return arrayOfNulls(size)
        }
    }


}