package com.example.juntavecinosapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Un Retrofit por microservicio. 10.0.2.2 = el PC visto desde el emulador
object RetrofitInstance {
    val adminApi: AdminApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9091/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdminApiService::class.java)
    }

    val reservasApi: ReservasApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9092/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReservasApiService::class.java)
    }

    val finanzasApi: FinanzasApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9093/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FinanzasApiService::class.java)
    }

    val indicadoresApi: IndicadoresApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://mindicador.cl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndicadoresApiService::class.java)
    }
}
