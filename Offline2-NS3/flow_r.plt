set terminal png
set output "STATIC_FlowVsRatio.png"
set title "FlowVsRatio"
set xlabel "Flow"
set ylabel "Ratio"
plot "Static_offline_output/flow.dat" using 2:6 with lines title "Flow/Ratio"
