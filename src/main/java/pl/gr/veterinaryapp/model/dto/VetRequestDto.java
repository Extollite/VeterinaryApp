package pl.gr.veterinaryapp.model.dto;

import lombok.Data;

import java.time.OffsetTime;

@Data
public class VetRequestDto {

    private String name;
    private String surname;
    private String photoUrl;
    private OffsetTime workStartTime;
    private OffsetTime workEndTime;

    public void setWorkStartTime(OffsetTime workStartTime) {
        this.workStartTime = workStartTime.withOffsetSameInstant(OffsetTime.now().getOffset());
    }

    public void setWorkEndTime(OffsetTime workEndTime) {
        this.workEndTime = workEndTime.withOffsetSameInstant(OffsetTime.now().getOffset());
    }
}
