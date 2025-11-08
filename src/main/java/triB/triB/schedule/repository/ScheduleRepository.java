package triB.triB.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.schedule.entity.Schedule;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * 특정 여행의 모든 일정 조회 (일차, 방문 순서 정렬)
     */
    List<Schedule> findByTripIdOrderByDayNumberAscVisitOrderAsc(Long tripId);
}
