package hkust.edu.visualneo.utils.backend;

import java.util.Map;

public interface Mappable {

    String getName();

    Map<?, ?> toMap();
}
