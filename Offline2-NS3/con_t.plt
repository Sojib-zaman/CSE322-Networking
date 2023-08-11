set terminal png
set output "STATIC_CovAreaVsThroughput.png"
set title "CovAreaVsThroughput"
set xlabel "Coverage factor"
set ylabel "Throughput"
plot "Static_offline_output/coverage.dat" using 4:5 with lines title "Coverage-factor/Throughput"
