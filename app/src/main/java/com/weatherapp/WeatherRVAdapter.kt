package com.weatherapp

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.ParseException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.Locale

class WeatherRVAdapter(var context: Context, var weatherRVModalArrayList: ArrayList<WeatherRVModal>) : RecyclerView.Adapter<WeatherRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Update information onto weather rv item xml from our modal
        var modal = weatherRVModalArrayList[position]
        holder.temperatureTV.text = "${modal.temperature}°F"
        Picasso.get().load("http:${modal.icon}").into(holder.conditionIV)
        holder.windTV.text = "${modal.windSpeed} mph"

        // Time and date processing
        var input = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault())
        var output = SimpleDateFormat("hh:mm aa", Locale.getDefault())

        try {
            var t = input.parse(modal.time)
            holder.timeTV.text = output.format(t)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return weatherRVModalArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var windTV: TextView = itemView.findViewById(R.id.idTVWindSpeed)
        var temperatureTV: TextView = itemView.findViewById(R.id.idTVTemperature)
        var timeTV: TextView = itemView.findViewById(R.id.idTVTime)
        var conditionIV: ImageView = itemView.findViewById(R.id.idIVCondition)
    }
}




