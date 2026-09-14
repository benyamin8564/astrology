package com.astroresearch.app

import java.time.*
import kotlin.math.*

data class Body(val name:String, val lon:Double, val sign:Int, val degree:Double, val house:Int)
data class Chart(val bodies:List<Body>, val asc:Double, val mc:Double, val aspects:List<String>)

object AstroCalculator {
    val signs = arrayOf("حمل","ثور","جوزا","سرطان","اسد","سنبله","میزان","عقرب","قوس","جدی","دلو","حوت")
    private data class El(val N:Double,val i:Double,val w:Double,val a:Double,val e:Double,val M:Double)
    private fun norm(x:Double):Double { var v=x%360.0; if(v<0)v+=360.0; return v }
    private fun kepler(m0:Double,e:Double):Double { var E=toRad(norm(m0)); repeat(15){ E -= (E-e*sin(E)-toRad(norm(m0)))/(1-e*cos(E)) }; return E }
    private fun toRad(d:Double)=Math.toRadians(d)
    private fun toDeg(r:Double)=Math.toDegrees(r)
    private fun planetHelio(name:String,d:Double):DoubleArray {
        val e = when(name){
            "Mercury"->El(48.3313,7.0047,29.1241,.387098,.205635,168.6562)
            "Venus"->El(76.6799,3.3946,54.8910,.723330,.006773,48.0052)
            "Earth"->El(0.0,0.0,282.9404,1.0,.016709,356.0470)
            "Mars"->El(49.5574,1.8497,286.5016,1.523688,.093405,18.6021)
            "Jupiter"->El(100.4542,1.3030,273.8777,5.20256,.048498,19.8950)
            "Saturn"->El(113.6634,2.4886,339.3939,9.55475,.055546,316.9670)
            "Uranus"->El(74.0005,.7733,96.6612,19.18171,.047318,142.5905)
            "Neptune"->El(131.7806,1.7700,272.8461,30.05826,.008606,260.2471)
            else->El(0.0,0.0,0.0,1.0,0.0,0.0)
        }
        val N=toRad(e.N); val i=toRad(e.i); val w=toRad(e.w); val a=e.a; val ecc=e.e
        val M=toRad(norm(e.M + when(name){
            "Mercury"->4.0923344368*d; "Venus"->1.6021302244*d; "Earth"->.9856002585*d; "Mars"->.5240207766*d
            "Jupiter"->.0830853001*d; "Saturn"->.0334442282*d; "Uranus"->.011725806*d; "Neptune"->.005995147*d; else->0.0 }))
        val E=kepler(toDeg(M),ecc); val xv=a*(cos(E)-ecc); val yv=a*sqrt(1-ecc*ecc)*sin(E)
        val v=atan2(yv,xv); val r=hypot(xv,yv)
        val xh=r*(cos(N)*cos(v+w)-sin(N)*sin(v+w)*cos(i)); val yh=r*(sin(N)*cos(v+w)+cos(N)*sin(v+w)*cos(i)); val zh=r*(sin(v+w)*sin(i))
        return doubleArrayOf(xh,yh,zh)
    }
    private fun eclLon(x:Double,y:Double,z:Double):Double { val ob=toRad(23.4393); return norm(toDeg(atan2(y*cos(ob)-z*sin(ob),x))) }
    private fun julian(utc:ZonedDateTime):Double = utc.toEpochSecond()/86400.0 + 2440587.5
    private fun moonLon(d:Double, sunLon:Double):Double {
        val N=toRad(norm(125.1228-.0529538083*d)); val i=toRad(5.1454); val w=toRad(norm(318.0634+.1643573223*d)); val a=60.2666; val e=.0549; val M=norm(115.3654+13.0649929509*d)
        val E=kepler(M,e); val xv=a*(cos(E)-e); val yv=a*sqrt(1-e*e)*sin(E); val v=atan2(yv,xv); val r=hypot(xv,yv)
        val xh=r*(cos(N)*cos(v+w)-sin(N)*sin(v+w)*cos(i)); val yh=r*(sin(N)*cos(v+w)+cos(N)*sin(v+w)*cos(i)); val lon=norm(toDeg(atan2(yh,xh)))
        val Ls=sunLon; val Ms=norm(356.0470+.9856002585*d); val Lm=norm(M+toDeg(w)+toDeg(N)); val D=norm(Lm-Ls); val F=norm(Lm-toDeg(N))
        return norm(lon + (-1.274*sin(toRad(M-2*D)) + .658*sin(toRad(2*D)) - .186*sin(toRad(Ms)) - .059*sin(toRad(2*M-2*D)) - .057*sin(toRad(M-2*D+Ms)) + .053*sin(toRad(M+2*D)) + .046*sin(toRad(2*D-Ms)) + .041*sin(toRad(M-Ms)) - .035*sin(toRad(D)) - .031*sin(toRad(M+Ms)) - .015*sin(toRad(2*F-2*D)) + .011*sin(toRad(M-4*D))))
    }
    private fun gmst(jd:Double):Double { val T=(jd-2451545.0)/36525.0; return norm(280.46061837+360.98564736629*(jd-2451545.0)+.000387933*T*T-T*T*T/38710000.0) }
    private fun ascendant(jd:Double, lat:Double, lon:Double):Pair<Double,Double> {
        val theta=norm(gmst(jd)+lon); val eps=toRad(23.4393); val phi=toRad(lat)
        fun ra(l:Double)=atan2(sin(toRad(l))*cos(eps),cos(toRad(l)))
        fun dec(l:Double)=asin(sin(eps)*sin(toRad(l)))
        var best=0.0; var bestErr=1e9
        for(i in 0..3600){ val l=i/10.0; val dd=dec(l); val q=-tan(phi)*tan(dd); if(q>=-1 && q<=1){ val h=-acos(q); var err=abs((toRad(theta)-ra(l)-h+Math.PI)%(2*Math.PI)-Math.PI); if(err<bestErr){bestErr=err;best=l} } }
        var bestMc=0.0; var e=1e9
        for(i in 0..3600){ val l=i/10.0; var er=abs((ra(l)-toRad(theta)+Math.PI)%(2*Math.PI)-Math.PI); if(er<e){e=er;bestMc=l} }
        return best to bestMc
    }
    fun calculate(local:LocalDateTime, tzOffset:Double, lat:Double, lon:Double, houseSystem:String, sidereal:Boolean=false):Chart {
        val utc=local.minusMinutes((tzOffset*60).roundToInt().toLong()).atZone(ZoneOffset.UTC); val jd=julian(utc); val d=jd-2451543.5
        val earth=planetHelio("Earth",d); val sun=norm(eclLon(-earth[0],-earth[1],-earth[2])); val moon=moonLon(d,sun)
        val names=listOf("Mercury","Venus","Mars","Jupiter","Saturn","Uranus","Neptune"); val lons=linkedMapOf("خورشید" to sun,"ماه" to moon)
        for(n in names){ val p=planetHelio(n,d); lons[when(n){"Mercury"->"عطارد";"Venus"->"زهره";"Mars"->"مریخ";"Jupiter"->"مشتری";"Saturn"->"زحل";"Uranus"->"اورانوس";else->"نپتون"}]=eclLon(p[0]-earth[0],p[1]-earth[1],p[2]-earth[2]) }
        val (rawAsc,rawMc)=ascendant(jd,lat,lon); val shift=if(sidereal) 24.0 else 0.0; val asc=norm(rawAsc-shift); val mc=norm(rawMc-shift); val ascSign=floor(asc/30).toInt()
        val bodies=lons.map{(n,rawL)-> val l=norm(rawL-shift); val s=floor(l/30).toInt(); val h=when(houseSystem){"برابر (Equal)" -> floor(norm(l-asc)/30).toInt()+1; else -> ((s-ascSign+12)%12)+1}; Body(n,l,s,l%30,h)}
        val aspects=mutableListOf<String>(); val list=bodies
        for(i in list.indices) for(j in i+1 until list.size){ val diff=abs(norm(list[i].lon-list[j].lon).let{if(it>180)360-it else it}); val targets=listOf(0.0,60.0,90.0,120.0,180.0); val labels=listOf("هم‌نشینی","تسدیس","تربیع","تثلیث","مقابله"); for(k in targets.indices){ val orb=if(k==0||k==4)8.0 else 6.0; if(abs(diff-targets[k])<=orb){aspects.add("${list[i].name} ${labels[k]} ${list[j].name} — فاصله ${"%.2f".format(diff)}°");break} } }
        return Chart(bodies,asc,mc,aspects)
    }
    fun sign(l:Double)=signs[floor(norm(l)/30).toInt()]
    fun deg(l:Double)="${floor(norm(l)%30).toInt()}° ${((norm(l)%1)*60).roundToInt()}′"
    fun normalize(x:Double)=norm(x)
}
