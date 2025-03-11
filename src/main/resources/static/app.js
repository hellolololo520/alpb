document.addEventListener("DOMContentLoaded", function () {
  // 날씨 데이터 가져오기
  fetchWeatherData();

  // 위치 후보 데이터 가져오기
  fetchLocationCandidates();

  // 새로고침 버튼 이벤트 리스너
  document.getElementById("refresh-btn").addEventListener("click", function () {
    collectWeatherData().then(() => {
      if (document.getElementById("current-tab").classList.contains("active")) {
        fetchWeatherData();
      } else {
        // 미래 날씨 탭이 활성화된 경우, 현재 시간 + 2시간으로 초기화하고 데이터 새로고침
        initializeSelectedTime();
        fetchFutureWeatherData();
      }
    });
  });

  // 탭 전환 이벤트 리스너
  document.getElementById("current-tab").addEventListener("click", function () {
    document.getElementById("current-tab").classList.add("active");
    document.getElementById("future-tab").classList.remove("active");
    document.getElementById("current-weather-container").style.display = "grid";
    document.getElementById("future-weather-container").style.display = "none";
  });

  document.getElementById("future-tab").addEventListener("click", function () {
    document.getElementById("future-tab").classList.add("active");
    document.getElementById("current-tab").classList.remove("active");
    document.getElementById("future-weather-container").style.display = "block";
    document.getElementById("current-weather-container").style.display = "none";

    // 미래 날씨 탭으로 전환할 때 현재 시간 + 2시간으로 초기화
    initializeSelectedTime();
    fetchFutureWeatherData();
  });

  // 시간 선택 버튼 이벤트 리스너
  document.getElementById("prev-time").addEventListener("click", function () {
    changeSelectedTime(-1);
  });

  document.getElementById("next-time").addEventListener("click", function () {
    changeSelectedTime(1);
  });

  // 위치 추가 버튼 이벤트 리스너
  document
    .getElementById("add-location-btn")
    .addEventListener("click", function () {
      addNewLocation();
    });

  // 엔터 키로 위치 추가
  document
    .getElementById("new-location-name")
    .addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        addNewLocation();
      }
    });

  document
    .getElementById("new-location-nx")
    .addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        addNewLocation();
      }
    });

  document
    .getElementById("new-location-ny")
    .addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        addNewLocation();
      }
    });
});

// 선택된 시간 초기화 (현재 시간 + 2시간으로 설정)
function initializeSelectedTime() {
  const now = new Date();
  const currentHour = now.getHours();
  // 현재 시간 + 2시간 (현재 시간 + 1시간의 다음 시간)
  const futureHour = (currentHour + 2) % 24;
  const selectedTimeElement = document.getElementById("selected-time");

  // 날짜 계산 (자정을 넘어가면 내일 날짜)
  const selectedDate = new Date(now);
  if (currentHour + 2 >= 24) {
    selectedDate.setDate(selectedDate.getDate() + 1);
  }

  // 날짜와 시간 표시
  const dateStr = selectedDate.getDate() === now.getDate() ? "오늘" : "내일";
  selectedTimeElement.textContent = `${dateStr} ${String(futureHour).padStart(
    2,
    "0"
  )}:00`;

  // 시간 제한에 따라 버튼 활성화/비활성화 상태 업데이트
  updateTimeNavigationButtons();
}

// 시간 네비게이션 버튼 활성화/비활성화 상태 업데이트
function updateTimeNavigationButtons() {
  const selectedTimeElement = document.getElementById("selected-time");
  const timeText = selectedTimeElement.textContent;
  const isToday = timeText.includes("오늘");
  const selectedHour = parseInt(timeText.split(" ")[1].split(":")[0]);

  const now = new Date();
  const currentHour = now.getHours();
  // 현재 시간 + 1시간 (현재 날씨에서 보여주는 시간)
  const nextHour = (currentHour + 1) % 24;

  const prevButton = document.getElementById("prev-time");
  const nextButton = document.getElementById("next-time");

  // 이전 버튼 활성화/비활성화 - 현재 시간 + 1시간의 다음 시간보다 이전으로는 이동 불가
  const minHour = (nextHour + 1) % 24;
  if (isToday && selectedHour === minHour) {
    prevButton.disabled = true;
    prevButton.classList.add("disabled-btn");
  } else {
    prevButton.disabled = false;
    prevButton.classList.remove("disabled-btn");
  }

  // 다음 버튼 활성화/비활성화 - 현재 시간 + 1시간 + 24시간에 도달하면 이동 불가
  const tomorrow = new Date(now);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const isTomorrowMaxReached = !isToday && selectedHour === nextHour;

  if (isTomorrowMaxReached) {
    nextButton.disabled = true;
    nextButton.classList.add("disabled-btn");
  } else {
    nextButton.disabled = false;
    nextButton.classList.remove("disabled-btn");
  }

  console.log(
    `현재 시간: ${currentHour}시, 다음 시간: ${nextHour}시, 선택된 시간: ${selectedHour}시, 오늘?: ${isToday}`
  );
  console.log(
    `이전 버튼 비활성화: ${prevButton.disabled}, 다음 버튼 비활성화: ${nextButton.disabled}`
  );
}

// 선택된 시간 변경
function changeSelectedTime(direction) {
  const selectedTimeElement = document.getElementById("selected-time");
  const timeText = selectedTimeElement.textContent;
  const isToday = timeText.includes("오늘");
  const selectedHour = parseInt(timeText.split(" ")[1].split(":")[0]);

  // 현재 시간 구하기
  const now = new Date();
  const currentHour = now.getHours();
  // 현재 시간 + 1시간 (현재 날씨에서 보여주는 시간)
  const nextHour = (currentHour + 1) % 24;

  // 새 시간과 날짜 계산
  let newHour = (selectedHour + direction) % 24;
  let isNewToday = isToday;

  // 날짜 변경 처리
  if (direction > 0 && selectedHour === 23) {
    // 오늘 23시에서 다음날 00시로 이동
    isNewToday = false;
  } else if (direction < 0 && selectedHour === 0 && !isToday) {
    // 내일 00시에서 오늘 23시로 이동
    isNewToday = true;
  }

  // 시간 제한 적용
  if (direction < 0) {
    // 이전 시간으로 이동할 때 현재 시간 + 1시간의 다음 시간보다 이전이면 제한
    const minHour = (nextHour + 1) % 24;
    if (isNewToday && newHour < minHour) {
      newHour = minHour;
      isNewToday = true;
    }
  } else {
    // 다음 시간으로 이동할 때 현재 시간 + 1시간 + 24시간보다 이후면 최대 허용 시간으로 제한
    if (!isNewToday && newHour === nextHour) {
      // 내일 최대 허용 시간에 도달
      newHour = nextHour;
    }
  }

  // 시간 업데이트
  const dateStr = isNewToday ? "오늘" : "내일";
  selectedTimeElement.textContent = `${dateStr} ${String(newHour).padStart(
    2,
    "0"
  )}:00`;

  // 시간 제한에 따라 버튼 활성화/비활성화 상태 업데이트
  updateTimeNavigationButtons();

  // 새 시간으로 날씨 데이터 가져오기
  fetchFutureWeatherData();
}

// 미래 날씨 데이터 가져오기
async function fetchFutureWeatherData() {
  const futureWeatherContainer = document.getElementById("future-weather-data");
  futureWeatherContainer.innerHTML =
    '<p class="loading">날씨 데이터 로딩 중...</p>';

  try {
    // 기본 위치 목록 (항상 표시할 위치들)
    const defaultLocations = [
      "고양킨텍스",
      "김포공항",
      "여의도",
      "잠실한강공원",
      "수서역",
      "드론시험인증센터",
      "계양신도시",
    ];

    // 날씨 데이터 가져오기
    const response = await fetch("/api/weather-new");
    if (!response.ok) {
      throw new Error("날씨 데이터를 가져오는데 실패했습니다.");
    }

    const data = await response.json();
    console.log("가져온 날씨 데이터:", data.length);

    if (data.length === 0) {
      futureWeatherContainer.innerHTML = "<p>날씨 데이터가 없습니다.</p>";
      return;
    }

    // 선택된 시간과 날짜 구하기
    const selectedTimeElement = document.getElementById("selected-time");
    const timeText = selectedTimeElement.textContent;
    const isToday = timeText.includes("오늘");
    const selectedHour = parseInt(timeText.split(" ")[1].split(":")[0]);

    // 현재 날짜 구하기
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = String(now.getMonth() + 1).padStart(2, "0");
    const currentDay = String(now.getDate()).padStart(2, "0");

    // 내일 날짜 구하기
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowYear = tomorrow.getFullYear();
    const tomorrowMonth = String(tomorrow.getMonth() + 1).padStart(2, "0");
    const tomorrowDay = String(tomorrow.getDate()).padStart(2, "0");

    // 선택된 날짜 결정
    const targetDate = isToday
      ? `${currentYear}${currentMonth}${currentDay}`
      : `${tomorrowYear}${tomorrowMonth}${tomorrowDay}`;

    const targetTime = `${String(selectedHour).padStart(2, "0")}00`; // 시간을 HH00 형식으로 변환

    console.log(
      `선택된 날짜: ${targetDate}, 선택된 시간: ${targetTime}, 오늘?: ${isToday}`
    );

    // 날씨 데이터 표시
    futureWeatherContainer.innerHTML = "";

    // 위치별로 그룹화
    const groupedByLocation = {};
    data.forEach((weather) => {
      if (weather.location) {
        const locationName = weather.location.locationName;
        if (!groupedByLocation[locationName]) {
          groupedByLocation[locationName] = [];
        }
        groupedByLocation[locationName].push(weather);
      }
    });

    console.log("그룹화된 위치:", Object.keys(groupedByLocation));

    // 기본 위치 먼저 처리 (있는 경우에만)
    defaultLocations.forEach((locationName) => {
      if (
        groupedByLocation[locationName] &&
        groupedByLocation[locationName].length > 0
      ) {
        const locationWeather = groupedByLocation[locationName];

        // 선택된 날짜와 시간대의 데이터 찾기
        let targetWeather = locationWeather.find(
          (weather) =>
            weather.fcstDate === targetDate && weather.fcstTime === targetTime
        );

        // 정확한 시간대가 없으면 가장 가까운 시간대 찾기
        if (!targetWeather) {
          let minTimeDiff = Infinity;

          locationWeather.forEach((weather) => {
            if (!weather.fcstDate || !weather.fcstTime) return;

            // 예보 시간을 Date 객체로 변환
            const fcstDateTime = new Date(
              weather.fcstDate.substring(0, 4),
              parseInt(weather.fcstDate.substring(4, 6)) - 1,
              weather.fcstDate.substring(6, 8),
              weather.fcstTime.substring(0, 2),
              0
            );

            // 선택된 시간을 Date 객체로 변환
            const selectedDateTime = new Date(
              isToday ? currentYear : tomorrowYear,
              isToday ? now.getMonth() : tomorrow.getMonth(),
              isToday ? now.getDate() : tomorrow.getDate(),
              selectedHour,
              0
            );

            const timeDiff = Math.abs(fcstDateTime - selectedDateTime);

            if (timeDiff < minTimeDiff) {
              minTimeDiff = timeDiff;
              targetWeather = weather;
            }
          });
        }

        if (targetWeather) {
          futureWeatherContainer.appendChild(
            createWeatherCard(targetWeather, isToday)
          );
        } else if (locationWeather.length > 0) {
          // 적절한 시간대를 찾지 못했다면 첫 번째 데이터 사용
          futureWeatherContainer.appendChild(
            createWeatherCard(locationWeather[0], isToday)
          );
        }

        // 처리한 위치는 목록에서 제거
        delete groupedByLocation[locationName];
      } else {
        console.log(`${locationName}의 날씨 데이터가 없습니다.`);
        // 데이터가 없는 경우 빈 카드 표시
        const emptyCard = document.createElement("div");
        emptyCard.className = "weather-card";
        emptyCard.innerHTML = `
          <div class="location-name">${locationName}</div>
          <div class="forecast-date">${isToday ? "오늘" : "내일"} ${String(
          selectedHour
        ).padStart(2, "0")}:00</div>
          <div class="temperature">--°C</div>
          <div class="weather-info">
            <div>날씨 데이터를 찾을 수 없습니다.</div>
          </div>
        `;
        futureWeatherContainer.appendChild(emptyCard);
      }
    });

    // 나머지 위치 처리 (활성화된 위치)
    Object.keys(groupedByLocation).forEach((locationName) => {
      const locationWeather = groupedByLocation[locationName];

      // 선택된 날짜와 시간대의 데이터 찾기
      let targetWeather = locationWeather.find(
        (weather) =>
          weather.fcstDate === targetDate && weather.fcstTime === targetTime
      );

      // 정확한 시간대가 없으면 가장 가까운 시간대 찾기
      if (!targetWeather) {
        let minTimeDiff = Infinity;

        locationWeather.forEach((weather) => {
          if (!weather.fcstDate || !weather.fcstTime) return;

          // 예보 시간을 Date 객체로 변환
          const fcstDateTime = new Date(
            weather.fcstDate.substring(0, 4),
            parseInt(weather.fcstDate.substring(4, 6)) - 1,
            weather.fcstDate.substring(6, 8),
            weather.fcstTime.substring(0, 2),
            0
          );

          // 선택된 시간을 Date 객체로 변환
          const selectedDateTime = new Date(
            isToday ? currentYear : tomorrowYear,
            isToday ? now.getMonth() : tomorrow.getMonth(),
            isToday ? now.getDate() : tomorrow.getDate(),
            selectedHour,
            0
          );

          const timeDiff = Math.abs(fcstDateTime - selectedDateTime);

          if (timeDiff < minTimeDiff) {
            minTimeDiff = timeDiff;
            targetWeather = weather;
          }
        });
      }

      if (targetWeather) {
        futureWeatherContainer.appendChild(
          createWeatherCard(targetWeather, isToday)
        );
      } else if (locationWeather.length > 0) {
        // 적절한 시간대를 찾지 못했다면 첫 번째 데이터 사용
        futureWeatherContainer.appendChild(
          createWeatherCard(locationWeather[0], isToday)
        );
      }
    });
  } catch (error) {
    console.error("미래 날씨 데이터 가져오기 오류:", error);
    futureWeatherContainer.innerHTML = `<p class="error">오류: ${error.message}</p>`;
  }
}

// 날씨 데이터 가져오기 (현재 날씨용)
async function fetchWeatherData() {
  const weatherContainer = document.getElementById("current-weather-container");
  weatherContainer.innerHTML = '<p class="loading">날씨 데이터 로딩 중...</p>';

  try {
    // 기본 위치 목록 (항상 표시할 위치들)
    const defaultLocations = [
      "고양킨텍스",
      "김포공항",
      "여의도",
      "잠실한강공원",
      "수서역",
      "드론시험인증센터",
      "계양신도시",
    ];

    // 날씨 데이터 가져오기
    const response = await fetch("/api/weather-new");
    if (!response.ok) {
      throw new Error("날씨 데이터를 가져오는데 실패했습니다.");
    }

    const data = await response.json();
    console.log("가져온 날씨 데이터:", data.length);

    if (data.length === 0) {
      weatherContainer.innerHTML = "<p>날씨 데이터가 없습니다.</p>";
      return;
    }

    // 현재 날짜와 시간 구하기
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = String(now.getMonth() + 1).padStart(2, "0");
    const currentDay = String(now.getDate()).padStart(2, "0");

    // 다음 시간대 표시를 위해 현재 시간에 1을 더함
    const nextHour = now.getHours() + 1;
    const targetHour = String(nextHour % 24).padStart(2, "0");

    const currentDate = `${currentYear}${currentMonth}${currentDay}`;
    const targetTime = `${targetHour}00`; // 시간을 HH00 형식으로 변환

    console.log(`현재 날짜: ${currentDate}, 표시할 시간: ${targetTime}`);

    // 날씨 데이터 표시
    weatherContainer.innerHTML = "";

    // 위치별로 그룹화
    const groupedByLocation = {};
    data.forEach((weather) => {
      if (weather.location) {
        const locationName = weather.location.locationName;
        if (!groupedByLocation[locationName]) {
          groupedByLocation[locationName] = [];
        }
        groupedByLocation[locationName].push(weather);
      }
    });

    console.log("그룹화된 위치:", Object.keys(groupedByLocation));

    // 기본 위치 먼저 처리 (있는 경우에만)
    defaultLocations.forEach((locationName) => {
      if (
        groupedByLocation[locationName] &&
        groupedByLocation[locationName].length > 0
      ) {
        const locationWeather = groupedByLocation[locationName];

        // 다음 시간대의 데이터 찾기
        let targetWeather = null;

        // 먼저 정확히 다음 시간대의 데이터를 찾음
        targetWeather = locationWeather.find(
          (weather) =>
            weather.fcstDate === currentDate && weather.fcstTime === targetTime
        );

        // 정확한 시간대가 없으면 가장 가까운 미래 시간대 찾기
        if (!targetWeather) {
          let closestFutureTime = Infinity;

          locationWeather.forEach((weather) => {
            if (!weather.fcstDate || !weather.fcstTime) return;

            const fcstDateTime = new Date(
              weather.fcstDate.substring(0, 4),
              parseInt(weather.fcstDate.substring(4, 6)) - 1,
              weather.fcstDate.substring(6, 8),
              weather.fcstTime.substring(0, 2),
              0
            );

            const timeDiff = fcstDateTime - now;

            // 미래 시간대 중 가장 가까운 것 선택
            if (timeDiff > 0 && timeDiff < closestFutureTime) {
              closestFutureTime = timeDiff;
              targetWeather = weather;
            }
          });
        }

        // 미래 시간대도 없으면 가장 가까운 시간대 선택
        if (!targetWeather && locationWeather.length > 0) {
          let minTimeDiff = Infinity;

          locationWeather.forEach((weather) => {
            if (!weather.fcstDate || !weather.fcstTime) return;

            const fcstDateTime = new Date(
              weather.fcstDate.substring(0, 4),
              parseInt(weather.fcstDate.substring(4, 6)) - 1,
              weather.fcstDate.substring(6, 8),
              weather.fcstTime.substring(0, 2),
              0
            );

            const timeDiff = Math.abs(fcstDateTime - now);

            if (timeDiff < minTimeDiff) {
              minTimeDiff = timeDiff;
              targetWeather = weather;
            }
          });
        }

        if (targetWeather) {
          weatherContainer.appendChild(createWeatherCard(targetWeather));
        } else if (locationWeather.length > 0) {
          // 적절한 시간대를 찾지 못했다면 첫 번째 데이터 사용
          weatherContainer.appendChild(createWeatherCard(locationWeather[0]));
        }

        // 처리한 위치는 목록에서 제거
        delete groupedByLocation[locationName];
      } else {
        console.log(`${locationName}의 날씨 데이터가 없습니다.`);
        // 데이터가 없는 경우 빈 카드 표시
        const emptyCard = document.createElement("div");
        emptyCard.className = "weather-card";
        emptyCard.innerHTML = `
          <div class="location-name">${locationName}</div>
          <div class="forecast-date">데이터 없음</div>
          <div class="temperature">--°C</div>
          <div class="weather-info">
            <div>날씨 데이터를 찾을 수 없습니다.</div>
          </div>
        `;
        weatherContainer.appendChild(emptyCard);
      }
    });

    // 나머지 위치 처리 (활성화된 위치)
    Object.keys(groupedByLocation).forEach((locationName) => {
      const locationWeather = groupedByLocation[locationName];

      // 다음 시간대의 데이터 찾기 (위와 동일한 로직)
      let targetWeather = null;

      // 먼저 정확히 다음 시간대의 데이터를 찾음
      targetWeather = locationWeather.find(
        (weather) =>
          weather.fcstDate === currentDate && weather.fcstTime === targetTime
      );

      // 정확한 시간대가 없으면 가장 가까운 미래 시간대 찾기
      if (!targetWeather) {
        let closestFutureTime = Infinity;

        locationWeather.forEach((weather) => {
          if (!weather.fcstDate || !weather.fcstTime) return;

          const fcstDateTime = new Date(
            weather.fcstDate.substring(0, 4),
            parseInt(weather.fcstDate.substring(4, 6)) - 1,
            weather.fcstDate.substring(6, 8),
            weather.fcstTime.substring(0, 2),
            0
          );

          const timeDiff = fcstDateTime - now;

          // 미래 시간대 중 가장 가까운 것 선택
          if (timeDiff > 0 && timeDiff < closestFutureTime) {
            closestFutureTime = timeDiff;
            targetWeather = weather;
          }
        });
      }

      // 미래 시간대도 없으면 가장 가까운 시간대 선택
      if (!targetWeather && locationWeather.length > 0) {
        let minTimeDiff = Infinity;

        locationWeather.forEach((weather) => {
          if (!weather.fcstDate || !weather.fcstTime) return;

          const fcstDateTime = new Date(
            weather.fcstDate.substring(0, 4),
            parseInt(weather.fcstDate.substring(4, 6)) - 1,
            weather.fcstDate.substring(6, 8),
            weather.fcstTime.substring(0, 2),
            0
          );

          const timeDiff = Math.abs(fcstDateTime - now);

          if (timeDiff < minTimeDiff) {
            minTimeDiff = timeDiff;
            targetWeather = weather;
          }
        });
      }

      if (targetWeather) {
        weatherContainer.appendChild(createWeatherCard(targetWeather));
      } else if (locationWeather.length > 0) {
        // 적절한 시간대를 찾지 못했다면 첫 번째 데이터 사용
        weatherContainer.appendChild(createWeatherCard(locationWeather[0]));
      }
    });
  } catch (error) {
    console.error("날씨 데이터 가져오기 오류:", error);
    weatherContainer.innerHTML = `<p class="error">오류: ${error.message}</p>`;
  }
}

// 위치 후보 데이터 가져오기
async function fetchLocationCandidates() {
  const tableBody = document.querySelector("#location-table tbody");
  tableBody.innerHTML =
    '<tr><td colspan="2" class="loading">데이터 로딩 중...</td></tr>';

  try {
    const response = await fetch("/api/location-candidates");
    if (!response.ok) {
      throw new Error("위치 후보 데이터를 가져오는데 실패했습니다.");
    }

    const data = await response.json();

    if (data.length === 0) {
      tableBody.innerHTML =
        '<tr><td colspan="2">위치 후보 데이터가 없습니다.</td></tr>';
      return;
    }

    // 위치 후보 데이터 표시
    tableBody.innerHTML = "";
    data.forEach((candidate) => {
      const row = document.createElement("tr");
      if (candidate.active) {
        row.classList.add("active-row");
      }

      row.innerHTML = `
                <td>${candidate.locationName}</td>
                <td>
                    ${
                      candidate.active
                        ? `<button class="btn danger-btn" onclick="deactivateLocation(${candidate.id})">비활성화</button>`
                        : `<button class="btn success-btn" onclick="activateLocation(${candidate.id})">활성화</button>`
                    }
                </td>
            `;

      tableBody.appendChild(row);
    });
  } catch (error) {
    console.error("위치 후보 데이터 가져오기 오류:", error);
    tableBody.innerHTML = `<tr><td colspan="2">오류: ${error.message}</td></tr>`;
  }
}

// 위치 활성화
async function activateLocation(id) {
  try {
    // 버튼 비활성화 및 로딩 상태 표시
    const button = document.querySelector(
      `button[onclick="activateLocation(${id})"]`
    );
    if (button) {
      button.disabled = true;
      button.textContent = "처리 중...";
    }

    // 알림 표시
    const tableRow = button.closest("tr");
    const locationName = tableRow.querySelector("td:first-child").textContent;

    // 날씨 컨테이너에 로딩 메시지 추가 (현재 활성화된 탭에 따라)
    let activeContainer;
    if (document.getElementById("current-tab").classList.contains("active")) {
      activeContainer = document.getElementById("current-weather-container");
    } else {
      activeContainer = document.getElementById("future-weather-data");
    }
    activeContainer.innerHTML = `<p class="loading">${locationName}의 날씨 데이터를 가져오는 중...</p>`;

    // activate-with-weather API 호출
    const response = await fetch(
      `/api/location-candidates/${id}/activate-with-weather`,
      {
        method: "POST",
      }
    );

    if (!response.ok) {
      throw new Error("위치 활성화에 실패했습니다.");
    }

    // 데이터 새로고침
    await fetchLocationCandidates();

    // 현재 활성화된 탭에 따라 날씨 데이터 새로고침
    if (document.getElementById("current-tab").classList.contains("active")) {
      await fetchWeatherData();
    } else {
      await fetchFutureWeatherData();
    }
  } catch (error) {
    console.error("위치 활성화 오류:", error);
    alert(`오류: ${error.message}`);

    // 오류 발생 시 날씨 컨테이너에 오류 메시지 표시 (현재 활성화된 탭에 따라)
    let activeContainer;
    if (document.getElementById("current-tab").classList.contains("active")) {
      activeContainer = document.getElementById("current-weather-container");
    } else {
      activeContainer = document.getElementById("future-weather-data");
    }
    activeContainer.innerHTML = `<p class="error">오류: ${error.message}</p>`;
  }
}

// 위치 비활성화
async function deactivateLocation(id) {
  try {
    // 버튼 비활성화 및 로딩 상태 표시
    const button = document.querySelector(
      `button[onclick="deactivateLocation(${id})"]`
    );
    if (button) {
      button.disabled = true;
      button.textContent = "처리 중...";
    }

    const response = await fetch(`/api/location-candidates/${id}/deactivate`, {
      method: "POST",
    });

    if (!response.ok) {
      throw new Error("위치 비활성화에 실패했습니다.");
    }

    // 데이터 새로고침
    await fetchLocationCandidates();

    // 현재 활성화된 탭에 따라 날씨 데이터 새로고침
    if (document.getElementById("current-tab").classList.contains("active")) {
      await fetchWeatherData();
    } else {
      await fetchFutureWeatherData();
    }
  } catch (error) {
    console.error("위치 비활성화 오류:", error);
    alert(`오류: ${error.message}`);
  }
}

// 날씨 데이터 수집
async function collectWeatherData() {
  try {
    const response = await fetch("/api/weather-new/collect", {
      method: "POST",
    });

    if (!response.ok) {
      throw new Error("날씨 데이터 수집에 실패했습니다.");
    }

    return true;
  } catch (error) {
    console.error("날씨 데이터 수집 오류:", error);
    alert(`오류: ${error.message}`);
    return false;
  }
}

// 날씨 카드 생성
function createWeatherCard(weather, isToday) {
  const card = document.createElement("div");
  card.className = "weather-card";

  // 날짜 형식 변환
  const fcstDate = `${weather.fcstDate.substring(
    0,
    4
  )}-${weather.fcstDate.substring(4, 6)}-${weather.fcstDate.substring(6, 8)}`;
  const fcstTime = `${weather.fcstTime.substring(
    0,
    2
  )}:${weather.fcstTime.substring(2, 4)}`;

  // 현재 날짜/시간과 비교
  const now = new Date();
  const currentDate = `${now.getFullYear()}-${String(
    now.getMonth() + 1
  ).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;

  // 날짜 표시 (오늘/내일)
  let dateDisplay =
    isToday !== undefined
      ? isToday
        ? "오늘"
        : "내일"
      : fcstDate === currentDate
      ? "오늘"
      : "내일";

  // 풍향 한글 표현으로 변환
  const windDirectionText = getWindDirectionText(
    weather.windDirection ? weather.windDirection.code : null
  );

  // 풍향 화살표 가져오기
  const windDirectionArrow = getWindDirectionArrow(
    weather.windDirection ? weather.windDirection.code : null
  );

  // 풍속 한글 표현으로 변환
  const windSpeedText = getWindSpeedText(
    weather.windSpeed ? weather.windSpeed.code : null
  );

  // 하늘상태 이모지
  const skyConditionEmoji = getSkyConditionEmoji(
    weather.skyCondition ? weather.skyCondition.code : null
  );

  // 강수확률 처리 (0%일 때도 표시)
  const precipitationProb =
    weather.precipitationProb !== null &&
    weather.precipitationProb !== undefined
      ? weather.precipitationProb + "%"
      : "0%";

  // UAM 가능 여부 판단
  const uamStatus = checkUamStatus(
    weather.skyCondition ? weather.skyCondition.code : null,
    weather.windSpeed ? weather.windSpeed.code : null,
    weather.precipitationProb,
    weather.humidity
  );

  // UAM 상태에 따른 클래스 추가
  const uamStatusClass =
    uamStatus.status === "가능" ? "uam-available" : "uam-unavailable";

  card.innerHTML = `
        <div class="location-name">${
          weather.location ? weather.location.locationName : "알 수 없음"
        }</div>
        <div class="forecast-date">${dateDisplay} ${fcstTime}</div>
        <div class="temperature">${
          weather.temperature ? weather.temperature + "°C" : "N/A"
        }</div>
        <div class="weather-info">
            <div class="sky-condition">
                <span>${skyConditionEmoji} ${
    weather.skyCondition ? weather.skyCondition.description : "N/A"
  }</span>
            </div>
            <div class="wind-info">
                <span>바람: ${windSpeedText}, ${windDirectionText}${windDirectionArrow}</span>
            </div>
            <div class="precipitation">
                <span>강수확률: ${precipitationProb}</span>
            </div>
            <div class="humidity">
                <span>습도: ${
                  weather.humidity ? weather.humidity + "%" : "N/A"
                }</span>
            </div>
            <div class="uam-status ${uamStatusClass}" onclick="showUamStatusReason(this, '${
    uamStatus.reason
  }')">
                <span>UAM 운항 ${uamStatus.status}</span>
                <div class="uam-reason" style="display: none;">${
                  uamStatus.reason
                }</div>
            </div>
        </div>
    `;

  return card;
}

// 풍향을 한글로 변환
function getWindDirectionText(direction) {
  const directions = {
    N: "북풍",
    NE: "북동풍",
    E: "동풍",
    SE: "남동풍",
    S: "남풍",
    SW: "남서풍",
    W: "서풍",
    NW: "북서풍",
  };

  return directions[direction] || "풍향 없음";
}

// 풍향을 화살표로 변환 (바람이 불어오는 방향을 나타냄)
function getWindDirectionArrow(direction) {
  const arrows = {
    N: "↓", // 북쪽에서 불어오는 바람은 아래쪽 화살표
    NE: "↙", // 북동쪽에서 불어오는 바람은 좌하단 화살표
    E: "←", // 동쪽에서 불어오는 바람은 왼쪽 화살표
    SE: "↖", // 남동쪽에서 불어오는 바람은 좌상단 화살표
    S: "↑", // 남쪽에서 불어오는 바람은 위쪽 화살표
    SW: "↗", // 남서쪽에서 불어오는 바람은 우상단 화살표
    W: "→", // 서쪽에서 불어오는 바람은 오른쪽 화살표
    NW: "↘", // 북서쪽에서 불어오는 바람은 우하단 화살표
  };

  return arrows[direction] ? `(${arrows[direction]})` : "";
}

// 풍속을 한글로 변환 (WindSpeedService.java 기반)
function getWindSpeedText(speedCode) {
  const speeds = {
    WEAK: "약함",
    MODERATE: "약간강함",
    STRONG: "강함",
  };

  return speeds[speedCode] || "풍속 정보 없음";
}

// 하늘상태 이모지 가져오기
function getSkyConditionEmoji(code) {
  const emojis = {
    1: "☀️", // 맑음
    3: "⛅", // 구름많음
    4: "☁️", // 흐림
  };

  return emojis[code] || "🌤️";
}

// UAM 가능 여부 확인 (이유 포함)
function checkUamStatus(skyCode, windSpeedCode, precipitationProb, humidity) {
  // 날씨가 맑음(코드 "1")인지 확인
  const isClear = skyCode === "1";

  // 풍속이 약함(코드 "WEAK")인지 확인
  const isWindWeak = windSpeedCode === "WEAK";

  // 강수확률이 50% 미만인지 확인 (null이나 undefined인 경우 0으로 처리)
  const precipProb =
    precipitationProb !== null && precipitationProb !== undefined
      ? precipitationProb
      : 0;
  const isLowPrecipitation = precipProb < 50;

  // 습도가 90% 미만인지 확인 (null이나 undefined인 경우 50으로 처리)
  const humidityValue =
    humidity !== null && humidity !== undefined ? humidity : 50;
  const isLowHumidity = humidityValue < 90;

  // 불가능 이유 생성
  let reason = "";
  if (!isClear) {
    reason += "날씨가 맑지 않음. ";
  }
  if (!isWindWeak) {
    reason += "바람이 강함. ";
  }
  if (!isLowPrecipitation) {
    reason += "강수확률이 높음. ";
  }
  if (!isLowHumidity) {
    reason += "습도가 높음. ";
  }

  // 모든 조건이 충족되면 가능, 아니면 불가능
  if (isClear && isWindWeak && isLowPrecipitation && isLowHumidity) {
    return { status: "가능", reason: "모든 조건 충족" };
  } else {
    return { status: "불가능", reason: reason.trim() };
  }
}

// UAM 상태 이유 표시 함수
function showUamStatusReason(element, reason) {
  const reasonElement = element.querySelector(".uam-reason");
  if (reasonElement.style.display === "none") {
    reasonElement.style.display = "block";
  } else {
    reasonElement.style.display = "none";
  }
}

// 새 위치 추가
async function addNewLocation() {
  const locationName = document
    .getElementById("new-location-name")
    .value.trim();
  const nx = document.getElementById("new-location-nx").value.trim();
  const ny = document.getElementById("new-location-ny").value.trim();

  // 입력 검증
  if (!locationName) {
    alert("위치명을 입력해주세요.");
    document.getElementById("new-location-name").focus();
    return;
  }

  if (!nx || !ny) {
    alert("X, Y 좌표를 모두 입력해주세요.");
    if (!nx) document.getElementById("new-location-nx").focus();
    else document.getElementById("new-location-ny").focus();
    return;
  }

  try {
    // 버튼 비활성화 및 로딩 상태 표시
    const addBtn = document.getElementById("add-location-btn");
    addBtn.disabled = true;
    addBtn.textContent = "추가 중...";

    // API 호출
    const response = await fetch("/api/location-candidates", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        locationName: locationName,
        nx: parseInt(nx),
        ny: parseInt(ny),
        active: true,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "위치 추가에 실패했습니다.");
    }

    // 입력 필드 초기화
    document.getElementById("new-location-name").value = "";
    document.getElementById("new-location-nx").value = "";
    document.getElementById("new-location-ny").value = "";

    // 데이터 새로고침
    await fetchLocationCandidates();

    // 현재 활성화된 탭에 따라 날씨 데이터 새로고침
    if (document.getElementById("current-tab").classList.contains("active")) {
      await fetchWeatherData();
    } else {
      await fetchFutureWeatherData();
    }

    // 성공 메시지
    alert(`${locationName} 위치가 추가되었습니다.`);
  } catch (error) {
    console.error("위치 추가 오류:", error);
    alert(`오류: ${error.message}`);
  } finally {
    // 버튼 상태 복원
    const addBtn = document.getElementById("add-location-btn");
    addBtn.disabled = false;
    addBtn.textContent = "위치 추가";
  }
}
