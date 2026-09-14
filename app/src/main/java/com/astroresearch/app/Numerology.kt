package com.astroresearch.app

object Numerology {
    private val pyth = mapOf(
        'A' to 1,'J' to 1,'S' to 1,'B' to 2,'K' to 2,'T' to 2,'C' to 3,'L' to 3,'U' to 3,
        'D' to 4,'M' to 4,'V' to 4,'E' to 5,'N' to 5,'W' to 5,'F' to 6,'O' to 6,'X' to 6,
        'G' to 7,'P' to 7,'Y' to 7,'H' to 8,'Q' to 8,'Z' to 8,'I' to 9,'R' to 9)
    private val abjad = mapOf('ا' to 1,'آ' to 1,'أ' to 1,'إ' to 1,'ب' to 2,'ج' to 3,'د' to 4,'ه' to 5,'ة' to 5,'و' to 6,'ز' to 7,'ح' to 8,'ط' to 9,'ی' to 10,'ي' to 10,'ک' to 20,'ك' to 20,'ل' to 30,'م' to 40,'ن' to 50,'س' to 60,'ع' to 70,'ف' to 80,'ص' to 90,'ق' to 100,'ر' to 200,'ش' to 300,'ت' to 400,'ث' to 500,'خ' to 600,'ذ' to 700,'ض' to 800,'ظ' to 900,'غ' to 1000)
    fun reduce(n:Int):Int { var x=kotlin.math.abs(n); while(x>9 && x!=11 && x!=22 && x!=33){ x=x.toString().sumOf{it-'0'} }; return x }
    fun lifePath(date:java.time.LocalDate):Int = reduce(date.year + date.monthValue + date.dayOfMonth)
    fun personalYear(date:java.time.LocalDate, targetYear:Int=date.year):Int = reduce(date.dayOfMonth + date.monthValue + targetYear)
    fun birthday(date:java.time.LocalDate)=reduce(date.dayOfMonth)
    fun expression(name:String):Pair<Int,String>{
        val latin=name.uppercase().filter{pyth.containsKey(it)}
        if(latin.isNotEmpty()) return reduce(latin.sumOf{pyth[it]?:0}) to "فیثاغورثی/لاتین"
        return reduce(name.filter{abjad.containsKey(it)}.sumOf{abjad[it]?:0}) to "ابجد"
    }
    fun soul(name:String):Int{ val latin=name.uppercase().filter{pyth.containsKey(it)}; if(latin.isNotEmpty()){ val vowels="AEIOU"; return reduce(latin.filter{it in vowels}.sumOf{pyth[it]?:0}) }; return reduce(name.filter{it in "اوی"}.sumOf{abjad[it]?:0}) }
    fun personality(name:String):Int{ val latin=name.uppercase().filter{pyth.containsKey(it)}; if(latin.isNotEmpty()){ val vowels="AEIOU"; return reduce(latin.filter{it !in vowels}.sumOf{pyth[it]?:0}) }; return reduce(name.filter{it !in "اوی" && abjad.containsKey(it)}.sumOf{abjad[it]?:0}) }
    fun chineseAnimal(year:Int):String{ val a=arrayOf("موش","گاو","ببر","خرگوش","اژدها","مار","اسب","بز","میمون","خروس","سگ","خوک"); return a[Math.floorMod(year-4,12)] }
    fun chineseCompatible(year:Int):List<String>{ val a=arrayOf("موش","گاو","ببر","خرگوش","اژدها","مار","اسب","بز","میمون","خروس","سگ","خوک"); val idx=Math.floorMod(year-4,12); val good=listOf((idx+4)%12,(idx+8)%12); return good.map{a[it]} }
}
