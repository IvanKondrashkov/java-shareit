package ru.practicum.shareit.utils;

import java.time.LocalDateTime;
import com.google.gson.TypeAdapter;
import java.time.format.DateTimeFormatter;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime date) throws IOException {
        if (date == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(date.format(formatter));
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        return LocalDateTime.parse(jsonReader.nextString(), formatter);
    }
}
