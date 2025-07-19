package gatodev.pa4web.DTO;

import gatodev.pa4web.models.Academy;
import gatodev.pa4web.models.League;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FighterDTO {
    private Integer id;
    private String dni;
    private String fullName;
    private Integer age;
    private Double weight;
    private String gender;
    private String rank;         
    private String modality;     
    private String photo;        
    private Academy academy;    
    private League league;      
}
