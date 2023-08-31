

#ifndef TCP_ADAPTIVE_RENO_H
#define TCP_ADAPTIVE_RENO_H

#include "tcp-congestion-ops.h"
#include "tcp-westwood-plus.h"
#include "ns3/data-rate.h"
#include "ns3/event-id.h"
#include "ns3/tcp-recovery-ops.h"
#include "ns3/traced-value.h"


namespace ns3
{

class Time;
class TcpAdaptiveReno : public TcpWestwoodPlus 
{
  public:

    static TypeId GetTypeId();

    TcpAdaptiveReno();

    TcpAdaptiveReno(const TcpAdaptiveReno& sock);
    ~TcpAdaptiveReno() override;


    enum FilterType
    {
        NONE,
        TUSTIN
    };

    uint32_t GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight) override;

    void PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt) override;

    Ptr<TcpCongestionOps> Fork() override;

    void  CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked) override ;
    
    double EstimateCongestionLevel() ; 
    void EstimateIncWnd(Ptr<TcpSocketState> tcb) ; 

  private:

    double_t alpha = 0.85 ; 
    double_t scaling_factor = 1000 ; 
    
    double_t Winc ; 
    double_t WincMax; 
    double_t Wprob ; 
    double_t Wbase ; 

    Time rtt_j ; 
    Time rtt_congestion ; 
    Time rtt_min ; 
    Time rtt_congestion_prev ; 
    Time rtt_packet_loss ; 



    void UpdateAckedSegments(int acked);

    void EstimateBW(const Time& rtt, Ptr<TcpSocketState> tcb);
   

  protected:

};

} 

#endif 
