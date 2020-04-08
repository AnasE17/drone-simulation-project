// Databricks notebook source
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{min, max}
import org.apache.spark.sql.Row

val spark = SparkSession
  .builder()
  .appName("Spark SQL basic example")
  .config("spark.some.config.option", "some-value")
  .getOrCreate()

// For implicit conversions like converting RDDs to DataFrames
import spark.implicits._

// COMMAND ----------

val df = spark.read.csv("File_Name.csv")

df.show()
display(df)

// COMMAND ----------

df.filter($"Vehicle Year" > 2012).show()

// COMMAND ----------

// quelles sont les types d'alertes les plus fréquentes
df.groupBy("Violation Code").count().show()


// COMMAND ----------

// quel sont les periodes ou il y a le plus/le moins d'alerte 
df.groupBy("Violation Time").count().show()


// COMMAND ----------

import org.apache.spark.sql.functions._

// On recherche la liste des drones qui renvoient le plus d'alertes
df.groupBy("Plate ID").count().sort(desc("count")).show()


// COMMAND ----------

df.columns(1) 


// on cherche les extremum des positions des drones pour la latitude 
df.agg(min(df.columns(43)), max(df.columns(43))).show()

// on cherche les extremum des positions des drones pour la longitude 
df.agg(min(df.columns(43)), max(df.columns(44))).show()

// COMMAND ----------

val latMax = df.agg(max(df.columns(43))).head().getDouble(0)
val latMin = df.agg(min(df.columns(43))).head().getDouble(0)
val longMax = df.agg(max(df.columns(44))).head().getDouble(0)
val longMin = df.agg(min(df.columns(44))).head().getDouble(0)


// On calcule un intervalle de 20% de la valeur seuil (difference entre les extremums) pour la latitude maximum
val t1 = (latMax-latMin)*0.2
val seuilLatMax = latMax-t1

// On calcule un intervalle de 20% de la valeur seuil (difference entre les extremums) pour la latitude minimum
val t2 = (latMax-latMin)*0.2
val seuilLatMin = latMin+t2

// On calcule un intervalle de 20% de la valeur seuil (difference entre les extremums) pour la longitude maximum
val t3 = (longMax-longMin)*0.2
val seuilLongMax = longMax-t3

// On calcule un intervalle de 20% de la valeur seuil (difference entre les extremums) pour la longitude minimum
val t4 = (longMax-longMin)*0.2
val seuilLongMin = longMin+t4

// On affiche les drones qui sont dans à la limite de la sortie du périmètre  
df.filter($"longitude" > seuilLongMax ||$"latitude" > seuilLatMax || $"longitude" < seuilLongMin ||$"latitude" < seuilLatMin  ).show()
display(df)

// COMMAND ----------


