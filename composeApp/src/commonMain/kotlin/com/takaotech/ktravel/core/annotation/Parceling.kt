package com.takaotech.ktravel.core.annotation

// For Android @Parcelize
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Parcelize

// For Android @IgnoreOnParcel
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
expect annotation class IgnoreOnParcel()

// For Android Parcelable
expect interface Parcelable
