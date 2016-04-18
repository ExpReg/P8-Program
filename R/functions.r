library(RSQLite)
library(functional)
require(RSQLite)

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
  plot(toPlot[,c(2,row)],  xlab = "Time \n s", ylab = "acceleration  m/s^2",main = name, col = 2 )
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

filterWihtLowPass <- function(data){
  time <- data[c(1,2)]
  data <- data[3:6]
  filteredData <- apply(data,2,function(x) lowPassFilter(x))
  return(data.frame(time,filteredData))
}

getTimeAll<- function(dataFrame){
  dataFrame[c(2,6)]
}

selectValues <- function(data){
  subset(data,allAxes < 0.5 & acc_y < 0.5 & acc_y > -0.1)
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


modifyTimeLine <- function(dataFrame){
  minFallTime <- min(selectValues(dataFrame)[,2])
  maxFallTime <- max(selectValues(dataFrame)[,2])
  newSet <- subset(dataFrame, relativeTime >= (minFallTime - 1) & relativeTime <= (maxFallTime + 1 ))
  newSet[,2] <- (newSet[,2] - newSet[,2][[1]])
  newSet
}

checkFrequency <- function(vect){
  len <- length(vect)
  Time <- max(vect)
  len/Time
}

checkFrequency2 <- function(vect){
  newVect <- vector()
  for(i in 1:(length(vect) - 1)){
    newVect[[i]] <- vect[[i + 1]] - vect[[i]] 
  }
  newVect
}

lowPassFilter <- function(data){
  vect <- vector()
  vect[[1]]<- data[[1]]
  for(i in 2:length(data)){
    vect[[i]] <- vect[[i - 1]] + (0.25 * (data[[i]] - vect[[i-1]]))
  }
  vect
}

plotLines <- function(data){
  plot(data[[1]],type = "n", ylim = c(-0.1,0.2),xlim = c(0.9,1.5))
  color <- c("black","blue","green","red","greenyellow")
  
  legend( x= "topright", y=0.92, 
          legend=c("1","2", "3", "4","5"), 
          col=c("black", "blue", "green", "red","greenyellow"),   
          pch=15)
  
  for(i in 1:length(data)){
    lines(data[[i]],col = color[[i]])
  }
  
}