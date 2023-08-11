set terminal png
set output "STATIC_CovAreaVsRatio.png"
set title "CovAreaVsRatio"
set xlabel "Coverage factor"
set ylabel "Ratio"
plot "Static_offline_output/coverage.dat" using 4:6 with lines title "Coverage-factor/Ratio"
