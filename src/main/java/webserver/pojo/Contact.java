package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    private Long contactId;
    private String title;
    private String firstName;
    private String lastName;
    private String corLanguage;
    private String country;
}
