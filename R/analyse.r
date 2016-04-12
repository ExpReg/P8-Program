library(RSQLite)
library(functional)
require(RSQLite)
#setwd("C:\\Users\\bjarke\\Desktop\\uni\\aau\\Dat 8\\Projekt\\P8-Program\\R")
setwd("C:\\Users\\Mads\\Documents\\GitHub\\P8-Program\\R")
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
plot(timeRelativetoStart,allAxes, xlab = "Time \n s", ylab = "acceleration  m/s^2",main = "AllAxes")


myConnection <- dbConnect(SQLite(), dbname="sliding.sqlite")

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


plotAllInOne(allTrips[[1]])
plotAll(allTrips[[1]])


#TRIP RELATED FUNCTIONS 
getAllTrips <- function(dbConnection){
  max <- (dbGetQuery(dbConnection, "SELECT max(trip) FROM sensor"))[[1]]
  myList <- list()
  for(i in 1:max){
    #Handle dates
    myList[[i]] = getTrip(dbConnection,i)
  }
  return(myList)
}

getTrip <- function(dbconnection,tripNr){
  #Handle dates
  dates <- dbGetQuery(dbconnection,paste("SELECT created_at FROM sensor WHERE trip =", tripNr,sep = ""))
  convertedDates <- apply(dates,1,function(x) as.numeric(strptime(x, "%Y-%m-%d %H:%M:%OS")))
  op <- options(digits.secs=3)
  relativeTime = round(convertedDates - convertedDates[1],digits = 3)
  
  #Handle accerlation 
  accerlationAxes <-  dbGetQuery(dbconnection,paste("SELECT acc_x,acc_z, acc_y FROM sensor WHERE trip = ",tripNr,sep = ""))
  x_values <- accerlationAxes[[1]]
  y_values <- accerlationAxes[[3]]
  z_values <- accerlationAxes[[2]]
  allAxes <- sqrt(x_values^2 + y_values^2 + z_values^2)
  return(data.frame(dates,relativeTime,accerlationAxes,allAxes))
}

#PlOT RELATED FUNCTIONS
plotStuff <- function(toPlot,name,row){
  plot(toPlot[,c(2,row)],  xlab = "Time \n s", ylab = "acceleration  m/s^2",main = name)
}

plotx <- Curry(plotStuff,name = "x-axis",row = 3)
ploty <- Curry(plotStuff,name = "y-axis",row = 5)
plotz <- Curry(plotStuff,name = "z-axis", row = 4)
plotAll <- Curry(plotStuff, name = "All axes",row = 6)

plotAllInOne <- function(toPlot){
  par(mfrow = c(2,2))
  plotx(toPlot)
  ploty(toPlot)
  plotz(toPlot)
  plotAll(toPlot)
  par(mfrow = c(1,1))
}


#USED FOR FILTERING
SMA <- function(data,k){
  myVect <- vector()
  for(i in seq(1,length(data), 1)){
    if(i <= k)
      myVect[i] <- sum(data[1:i])/i 
    else 
      myVect[i] <- sum(data[(i-(k-1)):i]) / k 
  }
  myVect[!is.na(myVect)]
}


filterDataFrame <- function(data,k){
  time <- data[c(1,2)]
  data <- data[3:6]
  filteredData <- apply(data,2,function(x) SMA(x,k))
  return(data.frame(time,filteredData))
}

getTimeAll<- function(dataFrame){
  dataFrame[c(2,6)]
}

selectValues <- function(data){
  subset(data,allAxes < 0.5)
}

determineFutureDiff <- function(data){
  fall <- selectValues(data)
  middleTime <- (max(fall[,1]) + min(fall[,1])) / 2
  past <- subset(data, relativeTime > (middleTime - 1) & relativeTime < middleTime)
  future <- subset(data, relativeTime < (middleTime  + 1 ) & relativeTime > middleTime)
  pastDiff <- max(past[,2]) - min(past[,2])
  futureDiff <- max(future[,2]) - min(future[,2])
  unlist(futureDiff)
}

determinePastDiff <-function(data){
  fall <- selectValues(data)
  middleTime <- (max(fall[,1]) + min(fall[,1])) / 2
  past <- subset(data, relativeTime > (middleTime - 1) & relativeTime < middleTime)
  future <- subset(data, relativeTime < (middleTime  + 1 ) & relativeTime > middleTime)
  pastDiff <- max(past[,2]) - min(past[,2])
  futureDiff <- max(future[,2]) - min(future[,2])
  unlist(pastDiff) 
}





