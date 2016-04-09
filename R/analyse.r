library(RSQLite)
require(RSQLite)
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



getAllTrips <- function(dbConnection){
  max <- (dbGetQuery(con, "SELECT max(trip) FROM sensor"))[[1]]
  myList <- list()
  for(i in 1:max){
    #Handle dates
    myList[[i]] = getTrip(con,i)
  }
  return(myList)
}

getTrip <- function(dbconnection,tripNr){
  #Handle dates
  dates <- dbGetQuery(con,paste("SELECT created_at FROM sensor WHERE trip =", tripNr,sep = ""))
  convertedDates <- apply(dates,1,function(x) as.numeric(strptime(x, "%Y-%m-%d %H:%M:%OS")))
  op <- options(digits.secs=3)
  relativeTime = round(convertedDates - convertedDates[1],digits = 3)
  
  #Handle accerlation 
  accerlationAxes <-  dbGetQuery(con,paste("SELECT acc_x,acc_z, acc_y FROM sensor WHERE trip = ",tripNr,sep = ""))
  x_values <- accerlationAxes[[1]]
  y_values <- accerlationAxes[[3]]
  z_values <- accerlationAxes[[2]]
  allAxes <- sqrt(x_values^2 + y_values^2 + z_values^2)
  return(data.frame(dates,relativeTime,accerlationAxes,allAxes))
}


