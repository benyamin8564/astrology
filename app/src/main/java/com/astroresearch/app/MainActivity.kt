package com.astroresearch.app

import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MainActivity : AppCompatActivity() {
    private lateinit var chartView: BirthChartView
    private lateinit var result: TextView
    private lateinit var houseSpinner: Spinner
    private lateinit var zodiacSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val name=findViewById<EditText>(R.id.name); val date=findViewById<EditText>(R.id.date); val time=findViewById<EditText>(R.id.time)
        val city=findViewById<EditText>(R.id.city); val country=findViewById<EditText>(R.id.country); val tz=findViewById<EditText>(R.id.tz)
        val lat=findViewById<EditText>(R.id.lat); val lon=findViewById<EditText>(R.id.lon); val risk=findViewById<EditText>(R.id.risk)
        chartView=findViewById(R.id.chartView); result=findViewById(R.id.result); houseSpinner=findViewById(R.id.houseSystem); zodiacSpinner=findViewById(R.id.zodiacSystem)
        val houses=arrayOf("Whole Sign","برابر (Equal)"); houseSpinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,houses)
        val zodiacs=arrayOf("غربی / Tropical","ودیک / Sidereal (Lahiri تقریبی)"); zodiacSpinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,zodiacs)
        findViewById<Button>(R.id.analyze).setOnClickListener {
            try {
                val d=parseBirthDate(date.text.toString()); val t=parseBirthTime(time.text.toString()); val dt=LocalDateTime.of(d,t)
                val tzv=parseTimezone(tz.text.toString()); val la=number(lat.text.toString()); val lo=number(lon.text.toString())
                require(la!=null && lo!=null){"عرض و طول جغرافیایی محل تولد را وارد کنید."}; require(la!! in -90.0..90.0){"عرض جغرافیایی باید بین -90 و 90 باشد."}; require(lo!! in -180.0..180.0){"طول جغرافیایی باید بین -180 و 180 باشد."}
                val sidereal=zodiacSpinner.selectedItemPosition==1
                val chart=AstroCalculator.calculate(dt,tzv,la,lo,houseSpinner.selectedItem.toString(),sidereal)
                chartView.chart=chart
                result.text=ReportBuilder.build(name.text.toString(),city.text.toString(),country.text.toString(),d,t,chart,zodiacSpinner.selectedItem.toString())
            } catch(e:Exception){ result.text="خطا در ورودی\n\n${e.message}\n\nتاریخ: 1990-01-25 یا 19900125\nساعت: 14:30 یا 1430\nمنطقه زمانی: +03:30 یا 3.5" }
        }
    }
    private fun number(s:String)=s.trim().replace(',','.').toDoubleOrNull()
    private fun parseBirthDate(raw:String):LocalDate{ val x=raw.trim().map{digit(it)}.joinToString("").replace('/','-').replace('.','-'); val digits=x.filter{it.isDigit()}; val n=if(digits.length==8&&!x.contains('-')) "${digits.substring(0,4)}-${digits.substring(4,6)}-${digits.substring(6,8)}" else x; return try{LocalDate.parse(n,DateTimeFormatter.ISO_LOCAL_DATE)}catch(_:DateTimeParseException){throw IllegalArgumentException("تاریخ تولد نامعتبر است.")} }
    private fun parseBirthTime(raw:String):LocalTime{ val x=raw.trim().map{digit(it)}.joinToString("").replace('.',':'); val digits=x.filter{it.isDigit()}; val n=if(digits.length==4&&!x.contains(':')) "${digits.substring(0,2)}:${digits.substring(2,4)}" else x; return try{LocalTime.parse(n,DateTimeFormatter.ofPattern("HH:mm"))}catch(_:Exception){throw IllegalArgumentException("ساعت تولد نامعتبر است.")} }
    private fun parseTimezone(raw:String):Double{ val x=raw.trim().replace(',','.'); require(x.isNotBlank()){"منطقه زمانی را وارد کنید."}; val sign=if(x.startsWith('-'))-1 else 1; val c=x.removePrefix("+").removePrefix("-"); val v=if(c.contains(':')){val p=c.split(':'); require(p.size==2); val h=p[0].toInt(); val m=p[1].toInt(); require(m in 0..59); h+m/60.0}else c.toDoubleOrNull()?:throw IllegalArgumentException("منطقه زمانی نامعتبر است."); require(sign*v in -14.0..14.0); return sign*v }
    private fun digit(c:Char)=when(c){'۰'->'0';'۱'->'1';'۲'->'2';'۳'->'3';'۴'->'4';'۵'->'5';'۶'->'6';'۷'->'7';'۸'->'8';'۹'->'9';'٠'->'0';'١'->'1';'٢'->'2';'٣'->'3';'٤'->'4';'٥'->'5';'٦'->'6';'٧'->'7';'٨'->'8';'٩'->'9';else->c}
}

object ReportBuilder {
    private val houseTopics=mapOf(1 to "هویت",2 to "پول و ارزش‌ها",3 to "یادگیری و ارتباط",4 to "خانه و ریشه‌ها",5 to "عشق و خلاقیت",6 to "کار و عادت‌ها",7 to "شراکت",8 to "منابع مشترک و ریسک",9 to "تحصیل و سفر",10 to "حرفه و اعتبار",11 to "شبکه و اهداف",12 to "خلوت و ناخودآگاه")
    private val traits=mapOf("حمل" to "ابتکار/رقابت","ثور" to "ثبات/منابع","جوزا" to "ارتباط/تنوع","سرطان" to "مراقبت/امنیت","اسد" to "خلاقیت/رهبری","سنبله" to "تحلیل/نظم","میزان" to "مذاکره/تعادل","عقرب" to "تمرکز/تحول","قوس" to "گسترش/تجربه","جدی" to "ساختار/هدف","دلو" to "نوآوری/شبکه","حوت" to "تخیل/همدلی")
    fun build(name:String,city:String,country:String,date:LocalDate,time:LocalTime,c:Chart,zodiac:String):String{
        val sb=StringBuilder(); sb.append("NATAL ASTROLOGY RESEARCH\n\n"); sb.append("نام: ${name.ifBlank{"—"}}\nتاریخ: $date   ساعت: $time\nمحل: ${city.ifBlank{"—"}}، ${country.ifBlank{"—"}}\nسیستم زودیاک: $zodiac\nسیستم خانه: Whole Sign / Equal قابل انتخاب\n\n")
        sb.append("════ جدول موقعیت‌ها ════\nسیاره | موقعیت | خانه | موضوع\n"); c.bodies.forEach{b->sb.append("${b.name} | ${AstroCalculator.sign(b.lon)} ${AstroCalculator.deg(b.lon)} | ${b.house} | ${houseTopics[b.house]}\n")}
        sb.append("ASC | ${AstroCalculator.sign(c.asc)} ${AstroCalculator.deg(c.asc)}\nMC  | ${AstroCalculator.sign(c.mc)} ${AstroCalculator.deg(c.mc)}\n\n════ جنبه‌ها ════\n"); if(c.aspects.isEmpty())sb.append("جنبه‌ای با اورب تعریف‌شده پیدا نشد.\n") else c.aspects.forEach{sb.append("• $it\n")}
        val lp=Numerology.lifePath(date); val ex=Numerology.expression(name); val py=Numerology.personalYear(date); val soul=Numerology.soul(name); val per=Numerology.personality(name)
        sb.append("\n════ نامورولوژی ════\nLife Path: $lp\nExpression: ${ex.first} (${ex.second})\nSoul Urge: $soul\nPersonality: $per\nBirthday: ${Numerology.birthday(date)}\nPersonal Year ${date.year}: $py\n\n")
        val animal=Numerology.chineseAnimal(date.year); val comp=Numerology.chineseCompatible(date.year).joinToString("، "); sb.append("════ زودیاک چینی ════\nسال تولد: $animal\nنشانه‌های سازگار سنتی: $comp\nاین رتبه‌بندی نمادین است و سازگاری واقعی افراد را تعیین نمی‌کند.\n\n")
        val moon=c.bodies.firstOrNull{it.name=="ماه"}?.lon?:0.0; val sid=AstroCalculator.normalize(moon-24.0); val nak=Math.floor(sid/(360.0/27.0)).toInt()+1; val nakNames=listOf("Ashwini","Bharani","Krittika","Rohini","Mrigashira","Ardra","Punarvasu","Pushya","Ashlesha","Magha","Purva Phalguni","Uttara Phalguni","Hasta","Chitra","Swati","Vishakha","Anuradha","Jyeshtha","Mula","Purva Ashadha","Uttara Ashadha","Shravana","Dhanishta","Shatabhisha","Purva Bhadrapada","Uttara Bhadrapada","Revati"); sb.append("════ ودیک / سیدریال ════\nماه سیدریال (Lahiri تقریبی): ${AstroCalculator.sign(sid)} ${AstroCalculator.deg(sid)}\nNakshatra: ${nakNames[nak-1]} ($nak/27)\n\n")
        sb.append("════ رابطه و ازدواج ════\nبرای مقایسه دقیق دو نفر باید تاریخ، ساعت و محل تولد نفر دوم نیز وارد شود. از این چارت به‌تنهایی نمی‌توان سال قطعی ازدواج تعیین کرد. برای بررسی نمادین، خانه 7، حاکم آن، زهره، ماه و جنبه‌های مرتبط باید همزمان بررسی شوند.\n\n")
        sb.append("════ مسیر شغلی ════\nشاخص‌های اصلی: خانه‌های 2/6/10 و سیارات حاضر در آن‌ها.\n"); c.bodies.filter{it.house==2||it.house==6||it.house==10}.forEach{sb.append("• ${it.name} در خانه ${it.house}: ${traits[AstroCalculator.sign(it.lon)]}. حوزه‌های پیشنهادی برای بررسی: ${careerSuggestions(it.name,it.house)}\n")}
        sb.append("\n════ مالی و سرمایه‌گذاری ════\nاین بخش فقط یک پروفایل نمادین از ریسک و سبک تصمیم‌گیری است؛ از آسترولوژی نمی‌توان سود یک سهم، رمزارز یا صندوق را تضمین کرد. خانه‌های 2/8/11 و وضعیت مشتری/زحل برای مطالعه سنتی بررسی می‌شوند.\n\n════ سلامت ════\nدر سنت‌های نجومی، نشانه‌ها و خانه‌های 1/6/12 به موضوعات بدنی نسبت داده می‌شوند؛ این برنامه بیماری آینده را تشخیص یا پیش‌بینی پزشکی نمی‌کند. برای پیشگیری واقعی، غربالگری و نظر پزشک معیار است.\n")
        return sb.toString()
    }
    private fun careerSuggestions(p:String,h:Int)=when(h){10->"مدیریت، کارآفرینی، رهبری، تخصص حرفه‌ای";6->"تحلیل، خدمات، سلامت/بهداشت به‌عنوان حوزه کاری، عملیات";2->"مالی، فروش، منابع، کسب‌وکار";else->"بررسی بیشتر"}
}
