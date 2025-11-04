package triB.triB.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "message_place_details",
        indexes = {

        }
)
public class MessagePlaceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_place_detail_id", unique = true, nullable = false)
    private Long messagePlaceDetailId;

    @JoinColumn(name = "message_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Message message;

    @Column(name = "place_id", nullable = false)
    private String placeId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "photo_url", nullable = true, columnDefinition = "TEXT")
    private String photoUrl;

}
