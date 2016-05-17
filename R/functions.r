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

myDTW <-function(data){
  raw <- data[,3]
  filtered <- data[,4]
  dtwed <- dtw(raw,filtered)
  dtwed$distance
}

kendallStuff <- function(data){
  raw <- data[,3]
  filtered <- data[,4]
  cor(raw,filtered,method="kendall")
}



getTrip <- function(dbconnection,tripNr){
  #Handle dates
  dates <- dbGetQuery(dbconnection,paste("SELECT created_at FROM sensor WHERE trip =", tripNr,sep = ""))
  convertedDates <- apply(dates,1,function(x) as.numeric(strptime(x, "%Y-%m-%d %H:%M:%OS")))
  op <- options(digits.secs=3)
  relativeTime = round(convertedDates - convertedDates[1],digits = 3)
  
  #Handle accerlation,acc_97,acc_98,acc_99 
  accerlationAxes <-  dbGetQuery(dbconnection,paste("SELECT acc_x,acc_y,acc_z FROM sensor WHERE trip = ",tripNr,sep = ""))
  x_values <- accerlationAxes[[1]]
  y_values <- accerlationAxes[[3]]
  z_values <- accerlationAxes[[2]]
  allAxes <- sqrt(x_values^2 + y_values^2 + z_values^2)
  return(data.frame(dates,relativeTime,accerlationAxes))
}

#PlOT RELATED FUNCTIONS
plotStuff <- function(toPlot,name,row){
  plot(toPlot[,c(2,row)],  xlab = "Time \n s", ylab = "acceleration  m/s^2",main = name, col = "black",pch 
       = 16)
}

plotx <- Curry(plotStuff,name = "x-axis",row = 3)
ploty <- Curry(plotStuff,name = "y-axis",row = 4)
plotz <- Curry(plotStuff,name = "z-axis", row = 5)
plotAll <- Curry(plotStuff, name = "All axes",row = 6)
plotGen <- Curry(plotStuff,name = "generic",row)

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
  data <- data[3:5]
  filteredData <- apply(data,2,function(x) lowPassFilter(x))
  return(data.frame(time,filteredData))
}

filterWihtLowPass2 <- function(data){
  time <- data[c(1,2)]
  data <- data[3:6]
  filteredData <- apply(data,2,function(x) lowPassFilter2(x))
  return(data.frame(time,filteredData))
}

getTimeAll<- function(dataFrame){
  dataFrame[c(2,6)]
}

selectValues <- function(data){
  subset(data,allAxes < 0.5 & acc_y <0 & acc_y > -0.2)
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


lowPassFilter2 <- function(data){
  vect <- vector()
  vect[[1]]<- data[[1]]
  for(i in 2:length(data)){
    vect[[i]] <- 0.8* vect[[i - 1]] + (0.2 * data[[i]])
  }
  vect <- data - vect
  vect
}

plotLines <- function(data){
  plot(data[[1]],type = "n", ylim = c(-0.14,0),xlim = c(0.9,1.5), xlab = "Time \n s", ylab = "acceleration  m/s^2",main = "1 Meter falls")
  color <- c("black", "blue", "green", "red","greenyellow","orange","midnightblue","seagreen", "steelblue", "tan4")
  
  legend( x= "topright", y=0.92, 
          legend=c("1","2", "3", "4","5","6","7","8","9","10"), 
          col=c("black", "blue", "green", "red","greenyellow","orange","midnightblue","seagreen", "steelblue", "tan4"), 
          pch=15)
  
  for(i in 1:length(data)){
    lines(data[[i]],col = color[[i]])
  }
}

myThing <- function(dataList){
  list <- list()
  for(i in 1:length(dataList[[1]])){
    list[[i]] <- unlist(lapply(dataList, function(x) x[[i]]))
  }
  return(list)
}

handleValues <- function(data){
  vect <- vector()
  for(i in 2:length(data)){
    if(i <= 100){
      vect[[i]] <- max(data[1:i]) - min(data[1:i])
    }
    else{
      vect[[i]] <- max(data[(i-100):i]) - min(data[(i-100):i])
    }
  }
  return(vect)
}



redoX <- function(data){
  before05 <- subset(data,relativeTime < 0.5)
  applied <- mean(before05[,3])
  test <- data[,3] - applied
  return(data.frame(data[,1:2],test,data[,4:8]))
}


duplicatedTime <- function(data){
  toRemove <- vector()
  for(i in 2:nrow(data)){   
    if(data[i,][2] == data[i - 1,][2]){
      meanx <- mean(c(data[i,][[3]], data[i-1,][[3]])) 
      meany <- mean(c(data[i,][[4]],data[i-1,][[4]]))
      meanz <- mean(c(data[i,][[5]],data[i-1,][[5]]))
      meanAll <- mean(c(data[i,][[6]],data[i-1,][[6]]))
      data[i,] <- c(data[i,][1:2],meanx,meany,meanz,meanAll)
      toRemove <- append(i-1,toRemove)
    }
  }
  if(length(toRemove) == 0){
    return(data)
  }
  return(data[-toRemove,])
  
}
myKendall <- function(data){
  apply(data[3:length(data)],2,function(x) cor(data[,3],x,method = "kendall"))
}

getKendallCol <- function(data,numCol){
  unlist(lapply(data, function(x) x[[numCol]]))
}

#dtw <- function(data1, data2){
#reps <- rep(c(Inf),length(data1) + length(data2) + 2)
#dtw <- matrix(reps,nrow = length(data1) + 1, ncol = length(data2) + 1)
#dtw[1,1] <- 0
#for(i in 2:length(data1)){
#  for(j in 2:length(data2)){
#    cost <- abs(data1[[i-1]] -  data2[[j-1]])
#    dtw[i,j] <- cost + min(dtw[i -1 , j],
#                           dtw[i,j-1],
#                           dtw[i-1,j-1])
#  }
#}
#return(dtw)
#}

#dtw(rep(5,5),rep(6,5))

