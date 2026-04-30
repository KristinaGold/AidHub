package com.example.data.dataStractures

import com.example.data.R

enum class Skill(val displayName: String, val iconResId: Int) {
    PLUMBING("Plumbing", R.drawable.skill_ic_plumbing),
    ELECTRICITY("Electricity", R.drawable.skill_ic_electricity),
    CAR_REPAIR("Car Repair",R.drawable.skill_ic_car_repair),
    PET_SITTING("Pet Sitting",R.drawable.skill_ic_pet_sitting),
    CLEANING("Cleaning",R.drawable.skill_ic_cleaning),
    TECH_SUPPORT("Tech Support",R.drawable.skill_ic_tech_support),
    GARDENING("Gardening",R.drawable.skill_ic_gardening),
    COOKING("Cooking",R.drawable.skill_ic_cooking),
    HANDYMAN("Handyman",R.drawable.skill_ic_handyman),
    LAUNDRY("Laundry",R.drawable.skill_ic_laundry),
    CAR_WASHING("Car Washing",R.drawable.skill_ic_car_wash),
    BABY_SITTING("Baby Sitting", R.drawable.skill_ic_baby_sitting),
    TUTORING("Tutoring", R.drawable.skill_ic_tutoring),
    MOVING("Moving", R.drawable.skill_ic_moving);

    companion object {
        fun fromDisplayName(name: String): Skill {
            return entries.find { it.displayName == name } ?: PLUMBING
        }

        fun getAllDisplayNames(): List<String> {
            return entries.map { it.displayName }
        }
    }
}