package com.example.model_one

data class gif(val phrase:String)


fun getGifList(): List<gif> {
    return listOf(
        gif("any question"),
        gif("are you hungry"),
        gif("are you busy"),
        gif("are you hungry"),
        gif("are you sick")
    )
}