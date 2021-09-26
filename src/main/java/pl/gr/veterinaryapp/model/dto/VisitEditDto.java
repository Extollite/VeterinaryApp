package pl.gr.veterinaryapp.model.dto;

import lombok.Data;
import pl.gr.veterinaryapp.common.VisitStatus;

@Data
public class VisitEditDto {

    private long id;
    private String description;
    private VisitStatus visitStatus;
}
