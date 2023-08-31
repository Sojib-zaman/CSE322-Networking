
#include "tcp-adaptive-reno.h"
#include "rtt-estimator.h"
#include "ns3/log.h"
#include "ns3/simulator.h"

NS_LOG_COMPONENT_DEFINE("TcpAdaptiveReno");
using namespace std ; 
namespace ns3
{

NS_OBJECT_ENSURE_REGISTERED(TcpAdaptiveReno);

TypeId
TcpAdaptiveReno::GetTypeId()
{
    static TypeId tid =
        TypeId("ns3::TcpAdaptiveReno")
            .SetParent<TcpWestwoodPlus>()
            .SetGroupName("Internet")
            .AddConstructor<TcpAdaptiveReno>();
            
    return tid;
}

TcpAdaptiveReno::TcpAdaptiveReno()
    : TcpWestwoodPlus(),
    Winc(0),
    WincMax(0),
    Wprob(0),
    Wbase(0),
    rtt_j(Time(0)),
    rtt_congestion(Time(0)),
    rtt_packet_loss(Time(0)),
    rtt_min(Time(0)),
    rtt_congestion_prev(Time(0))
{
    //NS_LOG_FUNCTION(this);
}

TcpAdaptiveReno::TcpAdaptiveReno(const TcpAdaptiveReno& sock)
    : TcpWestwoodPlus(sock),
    Winc(0),
    WincMax(0),
    Wprob(0),
    Wbase(0),
    rtt_j(Time(0)),
    rtt_congestion(Time(0)),
    rtt_packet_loss(Time(0)),
    rtt_min(Time(0)),
    rtt_congestion_prev(Time(0))  
{
    //NS_LOG_FUNCTION(this);
    NS_LOG_LOGIC("Invoked the copy constructor");
}

TcpAdaptiveReno::~TcpAdaptiveReno()
{
}

void
TcpAdaptiveReno::PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt)
{
   // NS_LOG_FUNCTION(this << tcb << packetsAcked << rtt);
    if(rtt.IsZero()) return ; 
    m_ackedSegments+=packetsAcked ; 

    // update the current rtt value 
    rtt_j = rtt ; 

    if(rtt_min.IsZero()) rtt_min = rtt ; 
    else if(rtt <= rtt_min) rtt_min = rtt ; 

    TcpWestwoodPlus::EstimateBW(rtt , tcb) ; 

}

double
TcpAdaptiveReno::EstimateCongestionLevel()
{
    if(rtt_congestion_prev<rtt_min) alpha = 0 ; 
    rtt_congestion = Seconds(alpha*rtt_congestion_prev.GetSeconds()+(1-alpha)*rtt_j.GetSeconds()) ; 
    double c , d , e; 
    d = rtt_j.GetSeconds() - rtt_min.GetSeconds() ;
    e = rtt_congestion.GetSeconds() - rtt_min.GetSeconds() ; 
    c = d/e ; 

    c=min(c,1.0) ; 
    return c; 
}

void 
TcpAdaptiveReno::EstimateIncWnd(Ptr<TcpSocketState> t)
{
    double_t segment_size =  t->m_segmentSize ; 
    double_t MSS = segment_size*segment_size ; 

    WincMax = m_currentBW.Get().GetBitRate() / (scaling_factor*MSS) ; 
    double c = EstimateCongestionLevel() ; 


    double a = 10 ; 
    double temp = 1/a ; 
    double b = 2*WincMax*(temp-(1+temp)/exp(a)) ; 
    double g = 1-2*WincMax*(temp-(0.5+temp)/exp(a)) ; 
    Winc = (WincMax/exp(c*a))+c*b+g ; 

}

uint32_t
TcpAdaptiveReno::GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight [[maybe_unused]])
{
    rtt_congestion_prev = rtt_congestion ; 
    rtt_packet_loss = rtt_j ; 
    double c = EstimateCongestionLevel() ; 
    Wbase = max((uint32_t)(tcb->m_cWnd/(1+c)) , 2*tcb->m_segmentSize); 
    Wprob = 0  ; 
    return Wbase ; 


}
void
TcpAdaptiveReno::CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
{
    if(segmentsAcked<=0) return;
    double_t segment_size =  tcb->m_segmentSize ; 
    double_t MSS = segment_size*segment_size ;
    Wbase+=(MSS/tcb->m_cWnd.Get()) ; 

    Wprob = max(Wprob+Winc/tcb->m_cWnd.Get() , 0.0 ) ;
    tcb->m_cWnd = Wbase+Wprob;  
     
}

Ptr<TcpCongestionOps>
TcpAdaptiveReno::Fork()
{
    return CreateObject<TcpAdaptiveReno>(*this);
}



} // namespace ns3
