package com.megogo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelDto {
    private String result;
    private List<DataDto> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataDto {
        private long id;
        @JsonProperty("external_id")
        private long externalId;
        private String title;
        private Map<String, String> pictures;
        @JsonProperty("video_id")
        private long videoId;
        private List<Program> programs;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Program {

            @JsonProperty("external_id")
            private int externalId;
            private String title;
            private Category category;
            private Map<String, String> pictures;
            @JsonProperty("start_timestamp")
            private long startTimestamp;
            @JsonProperty("end_timestamp")
            private long endTimestamp;
            private long id;
            private String start;
            private String end;
            @JsonProperty("virtual_object_id")
            private String virtualObjectId;
            @JsonProperty("schedule_type")
            private String scheduleType;
            @JsonProperty("video_id")
            private Integer videoId;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Category {
                private int id;
                @JsonProperty("external_id")
                private int externalId;
                private String title;
            }
        }
    }



}
