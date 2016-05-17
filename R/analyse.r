setwd("C:\\Users\\Mads\\Documents\\GitHub\\P8-Program\\R")
#setwd("C:\\Users\\bjarke\\Desktop\\uni\\aau\\Dat 8\\Projekt\\P8-Program\\R")
library(RSQLite)
library(functional)
library(ggplot2)
require(RSQLite)
source("functions.r")
install.packages("ggplot2")

#connection to database  
con = dbConnect(SQLite(),  dbname="1MeterFall.sqlite")
con2 = dbConnect(SQLite(), dbname ="angleTest.sqlite")
con3 = dbConnect(SQLite(), dbname = "ParentTrip.sqlite")
con4 = dbConnect(SQLite(), dbname ="15-05.sqlite")
con5 = dbConnect(SQLite(), dbname = "parkinglot.sqlite")

lol <- getAllTrips(con5)
lol <- lapply(lol,filterWihtLowPass)
lol<-lapply(lol, function(x) subset(x,relativeTime < 10))
hard <- lol[1:6]
soft <- lol[7:12]
moderate <- lol[12:17]

softs <- lapply(soft, function(x) max(x[,4]) -2.4 )
mean(unlist(softs))


moderates <- lapply(moderate,function(x) max(x[,4]) - 2.4)
mean(unlist(moderates))

ploty(soft[[1]])


hard <- lapply(hard,function(x) handleValues(x[,4]))
hards <- lapply(hard[2:6],function(x)max(x,na.rm = TRUE))
mean(unlist(hards))

#sundayTrip

tripper <- getAllTrips(con4)
lowpassed <- filterWihtLowPass(tripper[[1]])
from1307To1312 <- subset(lowpassed,relativeTime < 25*60 & relativeTime > 17 * 60)
plotStuff(from1307To1312,"97", 6)
plotx(from1307To1312)


from <- subset(from1307To1312,relativeTime >1220 & relativeTime < 1250)
plotStuff(from,"97", 8)
plotx(from)
from[nrow(from),]

aggrDriving <- subset(lowpassed,relativeTime > 33*60 & relativeTime < 38 * 60)
plotStuff(aggrDriving,"97", 8)
plotx(aggrDriving)

from2250 <- subset(aggrDriving,relativeTime > 2250 & relativeTime < 2265 )
plotStuff(from2250,"97", 8)
plotx(from2250)
from2250


from2030 <- subset(aggrDriving,relativeTime > 2030 & relativeTime < 2065)
plotStuff(from2030,"97", 8)
plotx(from2030)
from2250

from2150 <- subset(aggrDriving,relativeTime > 2100 & relativeTime <2150)
plotStuff(from2150,"97", 8)
plotx(from2150)


#FallTests 
falls <- getAllTrips(con)
plotAllInOne(falls[[1]])
modifiedGraphs <- lapply(fa
LIVE
 







lls, modifyTimeLine)
plotAllInOne(modifiedGraphs[[1]])
dropSelected <- lapply(modifiedGraphs, selectValues)
dropSelected <- lapply(dropSelected,function(x) subset(x,relativeTime > 1))
final <- lapply(dropSelected,duplicatedTime)
final2 <- lapply(final, filterWihtLowPass)

onlyY <- lapply(final2, function(x) x[,c(2,4)])
myAproxxes <- lapply(onlyY, function(x) approxfun(x[,1],x[,2]))
myValues <- lapply(myAproxxes, function(x) unlist(lapply(seq(1,1.36,0.02),x)))
df <- data.frame(matrix(unlist(myValues), nrow=length(myValues), byrow=T))
myValues
plotLines(onlyY)

plot(seq(1,1.36,0.02),meaned,type = "n")
lines(seq(1,1.36,0.02),meaned)

x_values <-seq(1,1.36,0.02)
n <-rep(10,length(x_values)) 
sded <- unlist(sded)
se <- sded / sqrt(10)
tgc <- data.frame(x_values,n,meaned,sded,se)

ggplot(tgc, aes(x=x_values, y=meaned)) + 
  geom_errorbar(aes(ymin=meaned-se, ymax=meaned+se), width=.01) +
  geom_line() +
  geom_point()


#30 Degrees 
con = dbConnect(SQLite(), dbname="30Degress.sqlite")
test <- getAllTrips(con)
handledRaw <- lapply(test, redoX)
mean(test[[1]][,3])
mystuff <- apply(test[[1]][,3:8], 2, redoX)
firstSet <- handledRaw[[1]]

#mean squraed error 
sqrt(mean((firstSet[,3] - firstSet[,4])^2))
#Mean absoulte error 
mean(abs(firstSet[,8] - firstSet[,3]))
y1 <- apply(firstSet[,4:8], 2, function(x) mean(abs(x - firstSet[,3])))

angles = getAllTrips(con2)
plotAllInOne(angles[[1]])
plotStuff(angles[[1]], "generic", 6)
firstAngle <- angles[[1]]
y2<- apply(firstAngle[,4:8], 2, function(x) mean(abs(x - rep(0,length(firstAngle[,3])))))
x1 <- seq(0.95,0.99,0.01)
mean(abs(firstAngle[,3] - rep(0,length(firstAngle[,3]))))


plot(x1,y1,type = "n")
lines(x1,y1)
lines(x1,y2)


test1 <- unique(mystuff)
t.test(test1[,1],test1[,2])
tripNr <-7 




test <- function(data){
vect <- vector()
  for(i in 1:length(data)){
    if(i <= 75){
       vect[[i]] <- max(data[1:i]) - min(data[1:i])
    }
    else{
      vect[[i]] <- max(data[(i-74):i]) - min(data[(i-74):i]) 
    }
  }
  return(vect) 
}

plotLines <- function(data){
  plot(data[[1]],type = "n", ylim = c(-0.1,0.5),xlim = c(0.9,1.5))
  color <- c("black","blue","green","red","greenyellow")
  
  legend( x= "topright", y=0.92, 
          legend=c("1","2", "3", "4","5"), 
          col=c("black", "blue", "green", "red","greenyellow"),   
          pch=15)
  
  for(i in 1:length(data)){
    lines(data[[i]],col = color[[i]])
  }
 
}

#btc 14042016: get falltimes and show them in a latex table
allTrips <- lapply(allTrips, unique)
dataFrame <- getDataFrame(allTrips, 28)
myFilteredDataFrames <- lapply(allTrips, selectValues)
res <- lapply(myFilteredDataFrames, getFallTime)
vecRes1m <- unlist(res[c(1,2,3,4,6)])
vecRes1m[[6]] <- mean(vecRes1m)
vecRes125m <- unlist(res[8:12])
vecRes125m[[6]] <- mean(vecRes125m)
vecRes15m <- unlist(res[13:17])
vecRes15m[[6]] <- mean(vecRes15m)
frameskk <- data.frame(vecRes1m, vecRes125m, vecRes15m)
createTimeTable(frameskk)




createTimeTable <- function(dataFrame){
  addtorow <- list()
  addtorow$pos <- list(0,0)
  addtorow$command <- c("iteration & 1.0 m & 1.25 m & 1.5 m \\\\\n", "& (n = 10) & (n = 10) & (n = 10) \\\\\n")
  print(xtable(dataFrame), add.to.row = addtorow, include.colnames = FALSE)  
}



getDataFrame<- function(tripNo){
  return(data.frame(allTrips[[tripNo]]$relativeTime,allTrips[[tripNo]]$allAxes))
}

getFallTime <- function(dataFrame){
  if(is.null(dataFrame))
    return("dataFrame is empty")
  else {
    fallTime = max(dataFrame[,2]) - min(dataFrame[,2])  
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






