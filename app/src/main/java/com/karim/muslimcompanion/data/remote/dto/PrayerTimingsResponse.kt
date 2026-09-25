package com.karim.muslimcompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Raw response shape returned by http://api.aladhan.com/v1/timings
 */
data class AladhanResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: AladhanData
)

data class AladhanData(
    @SerializedName("timings") val timings: TimingsDto,
    @SerializedName("date") val date: DateDto,
    @SerializedName("meta") val meta: MetaDto
)

data class TimingsDto(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Sunset") val sunset: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String,
    @SerializedName("Midnight") val midnight: String
)

data class DateDto(
    @SerializedName("readable") val readable: String,
    @SerializedName("gregorian") val gregorian: GregorianDto,
    @SerializedName("hijri") val hijri: HijriDto
)

data class GregorianDto(
    @SerializedName("date") val date: String
)

data class HijriDto(
    @SerializedName("date") val date: String,
    @SerializedName("month") val month: HijriMonthDto,
    @SerializedName("day") val day: String
)

data class HijriMonthDto(
    @SerializedName("en") val en: String,
    @SerializedName("ar") val ar: String
)

data class MetaDto(
    @SerializedName("timezone") val timezone: String
)
