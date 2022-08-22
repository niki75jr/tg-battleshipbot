package com.n75jr.battleship.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_games")
@Entity
public class UserGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_record_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User userRecordId;

    @Column(name = "opponent_id")
    private Long opponentId;

    @Column(name = "scores_game")
    private Short scoresGame;

    @Column(name = "total_shots")
    private Short totalShots;

    @Column(name = "is_win")
    private Boolean isWin;

    @CreationTimestamp
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}
