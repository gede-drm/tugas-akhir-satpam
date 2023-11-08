package com.geded.apartemenkusecurity

data class Unit(val id:Int, val unit_no:String){
    override fun toString(): String {
        return unit_no
    }
}
