#!/bin/bash

# general
filename="benchmark.ttl"
indent="  " # two spaces

bsbm="bsbm:"
hobbitKPI="hobbit:KPI"
kpiPrefix="hobbit:measuresKPI $bsbm"
query="qType"
explore="Explore"
update="Update"
business="Business"
range="rdfs:range"
double="xsd:double"
uint="xsd:unsignedInt"
int="xsd:integer"

labelPrefix="rdfs:label"
labelStart="queryType"
labelPostfix="\"@en;"


# KPI names for each query
success="Success"
successLabel="successful queries"
fail="Fail"
failLabel="failed queries"
minqet="MinQET"
minqetLabel="minimum query execution time"
maxqet="MaxQET"
maxqetLabel="maximum query execution time"
avgqet="AvgQET"
avgqetLabel="average query execution time"
avgqetgeo="AvgQETgeo"
avgqetgeoLabel="average query execution time (geometric)"
qps="QueriesPerSecond"
qpsLabel="queries per second"



# init 
# check if first argument (number of explore queries) is an integer
test $1 -ge 0 &> /dev/null
if [ $? -gt 1 ]
  then echo "use an integer as first argument"
  exit
elif [ $1 -lt 1 ]
  then echo "need number greater than 0"
  exit
fi

# check second argument (number of update queries)
test $2 -ge 0 &> /dev/null
if [ $? -gt 1 ]
  then echo "use an integer as second argument (may be 0)"
  exit
elif [ $2 -lt 0 ]
  then echo "need number greater ot equal to 0"
  exit
fi

# check third argument (number of business intelligence queries)
test $3 -ge 0 &> /dev/null
if [ $? -gt 1 ]
  then echo "use an integer as third argument (may be 0)"
  exit
elif [ $3 -lt 0 ]
  then echo "need number greater or equal to 0"
  exit
fi



# copy header.txt to file
cat header.txt > $filename

##################################################

# write measuresKPI for each explore query
numberOfQueries="$1"
loopCounter=1
while [ $loopCounter -le $numberOfQueries ] 
do
  prefix="$indent$kpiPrefix$query$explore$loopCounter"
  echo "$prefix$success;" >> $filename
  echo "$prefix$fail;" >> $filename
  echo "$prefix$minqet;" >> $filename
  echo "$prefix$maxqet;" >> $filename
  echo "$prefix$avgqet;" >> $filename
  echo "$prefix$avgqetgeo;" >> $filename
  echo "$prefix$qps;" >> $filename
  
  echo "" >> $filename
  ((loopCounter++))
done

# write measuresKPI for each update query
numberOfQueries="$2"
loopCounter=1
while [ $loopCounter -le $numberOfQueries ] 
do
  prefix="$indent$kpiPrefix$query$update$loopCounter"
  echo "$prefix$success;" >> $filename
  echo "$prefix$fail;" >> $filename
  echo "$prefix$minqet;" >> $filename
  echo "$prefix$maxqet;" >> $filename
  echo "$prefix$avgqet;" >> $filename
  echo "$prefix$avgqetgeo;" >> $filename
  echo "$prefix$qps;" >> $filename
  
  echo "" >> $filename
  ((loopCounter++))
done

# write measuresKPI for each business intelligence query
numberOfQueries="$3"
loopCounter=1
while [ $loopCounter -le $numberOfQueries ] 
do
  prefix="$indent$kpiPrefix$query$business$loopCounter"
  echo "$prefix$success;" >> $filename
  echo "$prefix$fail;" >> $filename
  echo "$prefix$minqet;" >> $filename
  echo "$prefix$maxqet;" >> $filename
  echo "$prefix$avgqet;" >> $filename
  echo "$prefix$avgqetgeo;" >> $filename
  echo "$prefix$qps;" >> $filename
  
  echo "" >> $filename
  ((loopCounter++))
done

##################################################

# copy middle.txt to file
cat middle.txt >> $filename

##################################################

# define range/type for each explore query
numberOfQueries="$1"
loopCounter=1
while [ $loopCounter -le $numberOfQueries ] 
do
  prefix="$bsbm$query$explore$loopCounter"
  if [ $loopCounter -le 9 ]
    then labelBegin="$indent$labelPrefix \"$labelStart$explore 0$loopCounter:"
    else labelBegin="$indent$labelPrefix \"$labelStart$explore $loopCounter:"
  fi

  echo "$prefix$success a $hobbitKPI;" >> $filename
  echo "$labelBegin (a) $successLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$fail a $hobbitKPI;" >> $filename
  echo "$labelBegin (b) $failLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$minqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (c) $minqetLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$maxqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (d) $maxqetLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$avgqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (e) $avgqetLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "$prefix$avgqetgeo a $hobbitKPI;" >> $filename
  echo "$labelBegin (f) $avgqetgeoLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "$prefix$qps a $hobbitKPI;" >> $filename
  echo "$labelBegin (g) $qpsLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "" >> $filename
  ((loopCounter++))
done

# define range/type for each update query
numberOfQueries="$2"
loopCounter=1
while [ $loopCounter -le $numberOfQueries ] 
do
  prefix="$bsbm$query$update$loopCounter"
  if [ $loopCounter -le 9 ]
    then labelBegin="$indent$labelPrefix \"$labelStart$update 0$loopCounter:"
    else labelBegin="$indent$labelPrefix \"$labelStart$update $loopCounter:"
  fi

  echo "$prefix$success a $hobbitKPI;" >> $filename
  echo "$labelBegin (a) $successLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$fail a $hobbitKPI;" >> $filename
  echo "$labelBegin (b) $failLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$minqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (c) $minqetLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$maxqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (d) $maxqetLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$avgqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (e) $avgqetLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "$prefix$avgqetgeo a $hobbitKPI;" >> $filename
  echo "$labelBegin (f) $avgqetgeoLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "$prefix$qps a $hobbitKPI;" >> $filename
  echo "$labelBegin (g) $qpsLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "" >> $filename
  ((loopCounter++))
done

# define range/type for each businessx intelligence query
numberOfQueries="$3"
loopCounter=1
while [ $loopCounter -le $numberOfQueries ] 
do
  prefix="$bsbm$query$business$loopCounter"
  if [ $loopCounter -le 9 ]
    then labelBegin="$indent$labelPrefix \"$labelStart$business 0$loopCounter:"
    else labelBegin="$indent$labelPrefix \"$labelStart$business $loopCounter:"
  fi

  echo "$prefix$success a $hobbitKPI;" >> $filename
  echo "$labelBegin (a) $successLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$fail a $hobbitKPI;" >> $filename
  echo "$labelBegin (b) $failLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$minqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (c) $minqetLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$maxqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (d) $maxqetLabel\";" >> $filename
  echo "$indent$range $int." >> $filename
  
  echo "$prefix$avgqet a $hobbitKPI;" >> $filename
  echo "$labelBegin (e) $avgqetLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "$prefix$avgqetgeo a $hobbitKPI;" >> $filename
  echo "$labelBegin (f) $avgqetgeoLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "$prefix$qps a $hobbitKPI;" >> $filename
  echo "$labelBegin (g) $qpsLabel\";" >> $filename
  echo "$indent$range $double." >> $filename
  
  echo "" >> $filename
  ((loopCounter++))
done

##################################################

# copy footer.txt to file
cat footer.txt >> $filename


# finish
echo "Generated $1 explore, $2 update and $3 business intelligence query KPIs."
echo "See <$filename>."

