package io.github.morgaroth.smogbilet

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by PRV on 28.01.2017.
 */
data class Info(val date: String, val text: String) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Info> = object : Parcelable.Creator<Info> {
            override fun createFromParcel(source: Parcel): Info = Info(source)
            override fun newArray(size: Int): Array<Info?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(date)
        dest?.writeString(text)
    }
}