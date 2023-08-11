set terminal png
set output "MobileFlowVsRatio.png"
set title "FlowVsRatio"
set xlabel "Flow"
set ylabel "Ratio"
plot "Mobile_offline_output/flow.dat" using 2:6 with lines title "Flow/Ratio"
