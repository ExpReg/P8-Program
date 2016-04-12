setwd("C:\\Users\\Mads\\Documents\\GitHub\\P8-Program\\R")
#setwd("C:\\Users\\bjarke\\Desktop\\uni\\aau\\Dat 8\\Projekt\\P8-Program\\R")
library(RSQLite)
library(functional)
require(RSQLite)
source("functions.r")

#connection to database  
con = dbConnect(SQLite(), dbname="07-04-2016.sqlite")

tripNr <-7 

#Queries 
dates <- dbGetQuery(con,paste("SELECT created_at FROM sensor WHERE trip =", tripNr,sep = ""))
accerlationAxes <-  dbGetQuery(con,paste("SELECT acc_x,acc_z, acc_y FROM sensor WHERE trip = ",tripNr,sep = ""))

#Convertetimestamp to numbers (apply is map )
convertedDates <- apply(dates,1,function(x) as.numeric(strptime(x, "%Y-%m-%d %H:%M:%OS")))
op <- options(digits.secs=3)

#The timestamps relative to the start time 
timeRelativetoStart = round(convertedDates - convertedDates[1],digits = 3)

x_values <- accerlationAxes[[1]]
y_values <- accerlationAxes[[3]]
z_values <- accerlationAxes[[2]]

##HAVE A DEMONSTRATION BJARKE (run the the whole thing then these plots/remember to run functions first)
filtered <- SMA(y_values,50)
filtertimeRelativetoStart <- timeRelativetoStart[seq(15,length(timeRelativetoStart),15)]
plot(timeRelativetoStart,y_values, xlab = "Time \n s", ylab = "acceleration  m/s^2",main = "y-axis")
plot(timeRelativetoStart,filtered, xlab = "Time \n s", ylab = "acceleration  m/s^2",main = "y-axis")
###################################

#defing main dataframe with for full interval
myDataFrame <- data.frame(timeRelativetoStart, y_values)
#dataFrame for interval: 300 to 420 sec
dataFrame300To420 <- myDataFrame[myDataFrame$timeRelativetoStart > 300 & 
                                myDataFrame$timeRelativetoStart < 420,]
plot(dataFrame300To420)
#total accerlation for all axes
allAxes <- sqrt(x_values^2 + y_values^2 + z_values^2)
#x-axis 
plot(timeRelativetoStart,x_values, xlab = "Time \n s", ylab = "acceleration  m/s^2",main = "x-axis")
#y-axis
plot(timeRelativetoStart,y_values, ylim=c(-2,2), xlab = "Time \n s", ylab = "acceleration  m/s^2",main = "y-axis")
#z-axis
plot(timeRelativetoStart,z_values, xlab = "Time \n s", ylab = "acceleration  m/s^2",main = "z-axis")
#Plot for allAxes 
plot(timeRelativetoStart,allAxes, xlab = "Time \n s", ylab = "acceleration  m/s^2",main = "AllAxes",xlim = c(0,3))


myConnection <- dbConnect(SQLite(), dbname="dunno.sqlite")

allTrips <- getAllTrips(myConnection)
OneMeter <- allTrips[c(1,2,3,4,6)]
Meter125 <- allTrips[c(8,9,10,11,12)]
Meter150 <- allTrips[c(13,14,15,16,17)]

#btc 10042016, added logic for filtering part of dataframe and finding the falltime for a trip
myDataFrameAllAxes <- getDataFrame(6) 
dataFrameAllAxes300To360 <- myDataFrameAllAxes[myDataFrameAllAxes$allTrips..tripNo...allAxes < 2,]
fallTime <- getFallTime(dataFrameAllAxes300To360$allTrips..tripNo...relativeTime) 

getDataFrame<- function(tripNo){
  return(data.frame(allTrips[[tripNo]]$relativeTime,allTrips[[tripNo]]$allAxes))
}

getFallTime <- function(dataFrame){
  if(is.null(dataFrame))
    return("dataFrame is empty")
  else {
    fallTime = max(dataFrame) - min(dataFrame)  
    return(fallTime)
  }
}


fall <- function(tripNr){
  allAxes <- getDataFrame(tripNr)
  allAxes2 <- allAxes[allAxes$allTrips..tripNo...allAxes < 0.5,]
  return(getFallTime(allAxes2$allTrips..tripNo...relativeTime))
}

allFalltimes <- function(trips){
  fallTimes <- vector()
  for(i in 1:length(trips)){
    fallTimes[[i]] <- fall(i)
  }
  return(fallTimes)
}






