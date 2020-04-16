package com.example.havadurumuapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tek_satir_layout.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*
import com.example.havadurumuapp.R.style.SpinnerGündüz as SpinnerGündüz


class MainActivity : AppCompatActivity() {

    var tvSehir:TextView? = null
    var location:SimpleLocation? = null
    var latitude:String? = null
    var longitude:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var adapter = ArrayAdapter.createFromResource(this,R.array.Sehirler,
            R.layout.tek_satir_layout)

        adapter.setDropDownViewResource(R.layout.tek_satir_layout)
        spinSehir.adapter = adapter

        spinSehir.setSelection(1)



        spinSehir.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                tvSehir = view as TextView

                location = SimpleLocation(this@MainActivity)

                if(position == 0){

                    if(!location!!.hasLocationEnabled()){
                        Toast.makeText(this@MainActivity,"GPS i aç ki yerini bulalım",Toast.LENGTH_LONG).show()
                        SimpleLocation.openSettings(this@MainActivity)



                    }else{

                        if(ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(this@MainActivity,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            60)
                        }else{

                            location = SimpleLocation(this@MainActivity)

                            latitude = String.format("%.2f",location?.latitude)
                            longitude = String.format("%.2f",location?.longitude)

                            Toast.makeText(this@MainActivity,"Lat:"+latitude+" lon:"+longitude,Toast.LENGTH_LONG).show()

                            currentSehir(latitude,longitude)

                        }

                    }

                }else{
                    var secilen = parent?.getItemAtPosition(position).toString()
                    verileriGetir(secilen)
                }

            }

        }

        spinSehir.setPositiveButton("Seç")

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 60){

            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                location = SimpleLocation(this@MainActivity)

                latitude = String.format("%.2f",location?.latitude)
                longitude = String.format("%.2f",location?.longitude)

                Toast.makeText(this@MainActivity,"Lat:"+latitude+" lon:"+longitude,Toast.LENGTH_LONG).show()

                currentSehir(latitude,longitude)
            }else{
                spinSehir.setSelection(1)
                Toast.makeText(this@MainActivity,"İzin vereydin de konumu bulaydık",Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun currentSehir(latitude: String?, longitude: String?) {

        val url = "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=fbc40783c28987a06bfed7213c3da9f0&lang=tr&units=metric"

        var sehirAdı:String? = null

        val havaDurumuObje2 = JsonObjectRequest(Request.Method.GET,url,null, object : Response.Listener<JSONObject>{


            override fun onResponse(response: JSONObject?) {

                var main = response?.getJSONObject("main")
                var sicaklik = main?.getInt("temp")

                tvSıcaklık.text = sicaklik?.toInt().toString()

                sehirAdı = response?.getJSONObject("name").toString()
                tvSehir?.setText(sehirAdı)

                var weather = response?.getJSONArray("weather")
                var aciklama = weather?.getJSONObject(0)?.getString("description")
                tvAcıklama.text = aciklama
                var iconResmi = weather?.getJSONObject(0)?.getString("icon")

                if(iconResmi?.last() == 'd'){
                    rootLayout.background = getDrawable(R.drawable.bg)
                    tvSıcaklık.setTextColor(resources.getColor(R.color.colorAccent))
                    tvAcıklama.setTextColor(resources.getColor(R.color.colorAccent))
                    tvTarih.setTextColor(resources.getColor(R.color.colorAccent))
                    tvDerece.setTextColor(resources.getColor(R.color.colorAccent))


                }else{
                    rootLayout.background = getDrawable(R.drawable.gece)
                    tvSıcaklık.setTextColor(resources.getColor(R.color.colorPrimary))
                    tvAcıklama.setTextColor(resources.getColor(R.color.colorPrimary))
                    tvTarih.setTextColor(resources.getColor(R.color.colorPrimary))
                    tvDerece.setTextColor(resources.getColor(R.color.colorPrimary))

                }

                var resimDosyaAdı = resources.getIdentifier("icon_"+iconResmi.sonKarakteriSil,"drawable",packageName)
                imgHavadurumu.setImageResource(resimDosyaAdı)

                tvTarih.text = tarihYazdır()


            }

        },object: Response.ErrorListener {

            override fun onErrorResponse(error: VolleyError?) {

            }


        })

        MySingleton.getInstance(this).addToRequestQueue(havaDurumuObje2)



    }


    fun verileriGetir(sehir:String){

        val url = "https://api.openweathermap.org/data/2.5/weather?q="+sehir+"&appid=fbc40783c28987a06bfed7213c3da9f0&lang=tr&units=metric"

        val havaDurumuObje = JsonObjectRequest(Request.Method.GET,url,null, object : Response.Listener<JSONObject>{

            override fun onResponse(response: JSONObject?) {

                var main = response?.getJSONObject("main")
                var sicaklik = main?.getInt("temp")

                tvSıcaklık.text = sicaklik?.toInt().toString()

                var weather = response?.getJSONArray("weather")
                var aciklama = weather?.getJSONObject(0)?.getString("description")
                tvAcıklama.text = aciklama
                var iconResmi = weather?.getJSONObject(0)?.getString("icon")

                if(iconResmi?.last() == 'd'){
                    rootLayout.background = getDrawable(R.drawable.bg)
                    tvSıcaklık.setTextColor(resources.getColor(R.color.colorAccent))
                    tvAcıklama.setTextColor(resources.getColor(R.color.colorAccent))
                    tvTarih.setTextColor(resources.getColor(R.color.colorAccent))
                    tvDerece.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSehir?.setTextColor(resources.getColor(R.color.colorAccent))

                }else{
                    rootLayout.background = getDrawable(R.drawable.gece)
                    tvSıcaklık.setTextColor(resources.getColor(R.color.colorPrimary))
                    tvAcıklama.setTextColor(resources.getColor(R.color.colorPrimary))
                    tvTarih.setTextColor(resources.getColor(R.color.colorPrimary))
                    tvDerece.setTextColor(resources.getColor(R.color.colorPrimary))
                    tvSehir?.setTextColor(resources.getColor(R.color.colorPrimary))
                }

                var resimDosyaAdı = resources.getIdentifier("icon_"+iconResmi.sonKarakteriSil,"drawable",packageName)
                imgHavadurumu.setImageResource(resimDosyaAdı)

                tvTarih.text = tarihYazdır()


            }

        },object: Response.ErrorListener {

            override fun onErrorResponse(error: VolleyError?) {

            }


        })

        MySingleton.getInstance(this).addToRequestQueue(havaDurumuObje)
    }

    fun tarihYazdır() : String{

        var takvim = Calendar.getInstance().time
        var formatlayıcı = SimpleDateFormat("EEEE ,MMMM yyyy",Locale("tr"))
        var tarih = formatlayıcı.format(takvim)

        return tarih

    }
    private val String?.sonKarakteriSil: String?
        get() {
            //50n yi 50 olarak geri yollar
            return this?.substring(0,(this?.length-1))
        }


}
