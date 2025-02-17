package admin.gpuserver.application;

import admin.gpuserver.domain.*;
import admin.gpuserver.domain.repository.GpuBoardRepository;
import admin.gpuserver.domain.repository.GpuServerRepository;
import admin.gpuserver.domain.repository.JobRepository;
import admin.gpuserver.domain.repository.LabUserRepository;
import admin.gpuserver.dto.request.GpuBoardRequest;
import admin.gpuserver.dto.request.GpuServerNameUpdateRequest;
import admin.gpuserver.dto.request.GpuServerRequest;
import admin.gpuserver.dto.response.GpuServerResponse;
import admin.gpuserver.dto.response.GpuServerResponses;
import admin.gpuserver.exception.GpuServerServiceException;
import admin.lab.domain.Lab;
import admin.lab.domain.repository.LabRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class GpuServerServiceTest {

    @Autowired
    private GpuServerService gpuServerService;

    @Autowired
    private GpuServerRepository gpuServerRepository;

    @Autowired
    private GpuBoardRepository gpuBoardRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private LabUserRepository labUserRepository;

    @Autowired
    private JobRepository jobRepository;

    private Lab lab;

    private GpuServer gpuServer1;
    private GpuServer gpuServer2;

    private GpuBoard gpuBoard1;
    private GpuBoard gpuBoard2;

    private LabUser labUser;

    private Job job1;
    private Job job2;
    private Job job3;
    private Job job4;

    @BeforeEach
    private void setUp() {
        lab = labRepository.save(new Lab("랩1"));

        gpuServer1 = gpuServerRepository.save(new GpuServer("GPU서버1", false, 600L, 1024L, lab));
        gpuServer2 = gpuServerRepository.save(new GpuServer("GPU서버2", true, 800L, 1024L, lab));

        gpuBoard1 = gpuBoardRepository.save(new GpuBoard(true, 800L, "aaa", gpuServer1));
        gpuServer1.setGpuBoard(gpuBoard1);

        gpuBoard2 = gpuBoardRepository.save(new GpuBoard(true, 800L, "bbb", gpuServer2));
        gpuServer2.setGpuBoard(gpuBoard2);

        labUser = labUserRepository.save(new LabUser("관리자1", UserType.MANAGER, lab));

        job1 = jobRepository.save(new Job("예약1", JobStatus.RUNNING));
        job2 = jobRepository.save(new Job("예약2", JobStatus.WAITING));
        job3 = jobRepository.save(new Job("예약3", JobStatus.WAITING));
        job4 = jobRepository.save(new Job("예약4", JobStatus.WAITING));

        gpuBoard1.setJobs(Arrays.asList(job1, job2, job3, job4));
    }

    @DisplayName("특정 GPU서버를 조회한다.")
    @Test
    void 특정_GPU_서버를_조회() {
        GpuServerResponse gpuServer = gpuServerService.findById(lab.getId(), gpuServer1.getId());
        assertThat(gpuServer).isNotNull();
    }

    @DisplayName("존재하지 Lab_ID로 GPU 서버를 조회한다.")
    @Test
    void 존재하지_않는_Lab_ID로_GPU_서버를_조회() {
        final Long nonexistentLabId = Long.MAX_VALUE;

        assertThatThrownBy(() -> gpuServerService.findById(nonexistentLabId, gpuServer1.getId()))
                .isInstanceOf(GpuServerServiceException.class)
                .hasMessage("Lab이 존재하지 않습니다.");
    }

    @DisplayName("존재하지 GPU_ID로 GPU 서버를 조회한다.")
    @Test
    void 존재하지_않는_GPU_ID로_GPU_서버를_조회() {
        final Long nonexistentServerId = Long.MAX_VALUE;

        assertThatThrownBy(() -> gpuServerService.findById(lab.getId(), nonexistentServerId))
                .isInstanceOf(GpuServerServiceException.class)
                .hasMessage("GPU 서버가 존재하지 않습니다.");
    }

    @DisplayName("삭제된 GPU_ID로 GPU 서버를 조회한다.")
    @Test
    void 삭제된_GPU_ID로_GPU_서버를_조회() {
        gpuServerService.delete(lab.getId(), gpuServer1.getId());
        assertThatThrownBy(() -> gpuServerService.findById(lab.getId(), gpuServer1.getId()))
                .isInstanceOf(GpuServerServiceException.class)
                .hasMessage("GPU 서버가 존재하지 않습니다.");
    }

    @DisplayName("삭제된 GPU 서버를 제외한 전체를 조회 한다.")
    @Test
    void 삭제된_GPU_서버를_제외한_전체_조회() {
        GpuServerResponses gpuServerResponses = gpuServerService.findAll(lab.getId());
        int beforeSize = gpuServerResponses.getGpus()
                .size();

        gpuServerService.delete(lab.getId(), gpuServer1.getId());

        GpuServerResponses gpuServers = gpuServerService.findAll(lab.getId());
        assertThat(gpuServers.getGpus()).hasSize(beforeSize - 1);
    }

    @DisplayName("존재하지 Lab_ID로 GPU 서버 전체를 조회한다")
    @Test
    void 존재하지_않는_Lab_ID로_전체_조회() {
        final Long nonexistentLabId = Long.MAX_VALUE;
        assertThatThrownBy(() -> gpuServerService.findAll(nonexistentLabId))
                .isInstanceOf(GpuServerServiceException.class)
                .hasMessage("Lab이 존재하지 않습니다.");
    }

    @DisplayName("GPU 서버의 이름을 수정한다.")
    @Test
    void 이름_수정() {
        GpuServerResponse gpuServer = gpuServerService.findById(lab.getId(), gpuServer1.getId());
        assertThat(gpuServer.getServerName()).isEqualTo("GPU서버1");

        GpuServerNameUpdateRequest gpuServerName = new GpuServerNameUpdateRequest("newGPU서버1");
        gpuServerService.updateGpuServer(gpuServerName, lab.getId(), gpuServer1.getId());

        gpuServer = gpuServerService.findById(lab.getId(), gpuServer1.getId());
        assertThat(gpuServer.getServerName()).isEqualTo("newGPU서버1");
    }

    @DisplayName("존재하지 Lab_ID로 GPU 서버의 이름을 수정한다.")
    @Test
    void 존재하지_않는_Lab_ID로_GPU_서버의_이름을_수정() {
        final Long nonexistentLabId = Long.MAX_VALUE;

        GpuServerNameUpdateRequest gpuServerName = new GpuServerNameUpdateRequest("newGPU서버1");
        assertThatThrownBy(() -> gpuServerService.updateGpuServer(gpuServerName, nonexistentLabId, gpuServer1.getId()))
                .isInstanceOf(GpuServerServiceException.class)
                .hasMessage("Lab이 존재하지 않습니다.");
    }

    @DisplayName("존재하지 GPU_ID로 GPU 서버의 이름을 수정한다.")
    @Test
    void 존재하지_않는_GPU_ID로_GPU_서버의_이름을_수정() {
        final Long nonexistentServerId = Long.MAX_VALUE;

        GpuServerNameUpdateRequest gpuServerName = new GpuServerNameUpdateRequest("newGPU서버1");
        assertThatThrownBy(() -> gpuServerService.updateGpuServer(gpuServerName, lab.getId(), nonexistentServerId))
                .isInstanceOf(GpuServerServiceException.class)
                .hasMessage("GPU 서버가 존재하지 않습니다.");
    }

    @DisplayName("삭제된 GPU_ID로 GPU 서버의 이름을 수정한다.")
    @Test
    void 삭제된_GPU_ID로_GPU_서버의_이름을_수정() {
        gpuServerService.delete(lab.getId(), gpuServer1.getId());
        GpuServerNameUpdateRequest gpuServerName = new GpuServerNameUpdateRequest("newGPU서버1");
        assertThatThrownBy(() -> gpuServerService.updateGpuServer(gpuServerName, lab.getId(), gpuServer1.getId()))
                .isInstanceOf(GpuServerServiceException.class)
                .hasMessage("GPU 서버가 존재하지 않습니다.");
    }

    @DisplayName("GPU 서버를 논리적으로 삭제하는 경우")
    @Test
    void deleteWithGpuId() {
        gpuServerService.delete(lab.getId(), gpuServer1.getId());
        GpuServer deletedGpuServer = gpuServerRepository.findById(gpuServer1.getId())
                .orElseThrow(IllegalArgumentException::new);
        assertTrue(deletedGpuServer.getDeleted());
    }

    @DisplayName("GPU 서버 삭제 과정에서 GPU ID를 찾을 수 없는 경우")
    @Test
    void deleteWithoutGpuId() {
        final Long nonexistentServerId = Long.MAX_VALUE;

        assertThrows(GpuServerServiceException.class, () -> gpuServerService.delete(lab.getId(), nonexistentServerId));
    }

    @DisplayName("GPU 서버 삭제 과정에서 해당 GPU 가 이미 논리적으로 삭제되어 있는 경우")
    @Test
    void logicalDeletedGpuServerDelete() {
        gpuServerService.delete(lab.getId(), gpuServer1.getId());

        assertThrows(GpuServerServiceException.class, () -> gpuServerService.delete(lab.getId(), gpuServer1.getId()));
    }

    @DisplayName("저장된 서버를 확인한다")
    @Test
    void saveServer() {
        GpuBoardRequest boardRequest = new GpuBoardRequest("nvdia", 10L);
        GpuServerRequest gpuServerRequest = new GpuServerRequest("server", 1L, 1L, boardRequest);

        Long gpuServerId = gpuServerService.saveGpuServer(gpuServerRequest, lab.getId());
        assertThat(gpuServerRepository.findById(gpuServerId)).isNotEmpty();
    }

    @DisplayName("gpuServer 저장 과정에서 labId가 없는 경우")
    @Test
    void saveServerWithoutLabId() {
        GpuBoardRequest boardRequest = new GpuBoardRequest("nvdia", 10L);
        GpuServerRequest gpuServerRequest = new GpuServerRequest("server", 1L, 1L, boardRequest);
        Long nonexistentLabId = Long.MAX_VALUE;

        assertThrows(GpuServerServiceException.class, () -> gpuServerService.saveGpuServer(gpuServerRequest, nonexistentLabId));
    }
}
