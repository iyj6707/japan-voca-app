package com.example.japanvocalist

import android.os.Parcel
import android.os.Parcelable

class CategoryDto(
    val id: Int,
    val name: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CategoryDto> {
        override fun createFromParcel(parcel: Parcel): CategoryDto {
            return CategoryDto(parcel)
        }

        override fun newArray(size: Int): Array<CategoryDto?> {
            return arrayOfNulls(size)
        }
    }
}