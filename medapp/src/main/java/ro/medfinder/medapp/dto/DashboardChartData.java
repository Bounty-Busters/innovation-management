package ro.medfinder.medapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardChartData {
    private List<String> lineLabels;
    private List<Integer> lineData;
    
    private List<String> doughnutLabels;
    private List<Integer> doughnutData;
    
    private List<String> barLabels;
    private List<Integer> barData;
    
    private List<String> pieLabels;
    private List<Integer> pieData;
}
