library(RSQLite)
require(RSQLite)
setwd("home/expreg/AndroidStudioProjects/P8-Program/R")

#connection to database  
con = dbConnect(SQLite(), dbname="test.sqlite")

tripNr <-2 

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

#total accerlation for all axes
allAxes <- sqrt(x_values^2 + y_values^2 + z_values^2)

#x-axis 
plot(timeRelativetoStart,x_values)
#y-axis
plot(timeRelativetoStart,y_values)
#z-axis
plot(timeRelativetoStart,z_values)

#Plot for allAxes 
plot(timeRelativetoStart,allAxes)
