package ro.medfinder.medapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardStats {
    private String label1;
    private String value1;
    
    private String label2;
    private String value2;
    
    private String label3;
    private String value3;
    
    private String label4;
    private String value4;
}
