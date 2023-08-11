set terminal png
set output "STATIC_nodeVsRatio.png"
set title "nodeVsRatio"
set xlabel "Number of nodes"
set ylabel "Ratio"
plot "Static_offline_output/node.dat" using 1:6 with lines title "Node/Ratio"
