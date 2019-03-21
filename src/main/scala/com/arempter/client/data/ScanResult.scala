package com.arempter.client.data

sealed trait ScanResult

case object ObjectClean extends ScanResult
case object ObjectInfected extends ScanResult
