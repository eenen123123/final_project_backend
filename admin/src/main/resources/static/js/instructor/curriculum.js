let gridApi = null;
let currentSelectedMasterId = null;

const gridOptions = {
    columnDefs: [
        {
            headerName: "#",
            valueGetter: "node.rowIndex + 1",
            width: 60,
            cellClass: "text-center text-slate-400 font-mono text-xs",
            headerClass: "text-center",
        },
        {
            headerName: "주차 및 회차",
            field: "weekInfo",
            width: 150,
            editable: true,
            checkboxSelection: true,
        },
        {
            headerName: "강의 주제",
            field: "topic",
            width: 220,
            editable: true,
        },
        {
            headerName: "상세 강의 내용",
            field: "content",
            flex: 1,
            editable: true,
            cellEditor: "agLargeTextCellEditor",
        },
    ],
    rowData: [],
    rowSelection: "single",
    defaultColDef: {
        resizable: true,
        sortable: false,
        filter: false,
    },
    suppressNoRowsOverlay: false,
    overlayNoRowsTemplate:
        '<div class="text-sm text-slate-400 py-8">행이 없습니다. 상단의 <b>행 추가</b> 버튼을 눌러 내용을 입력하세요.</div>',
};

document.addEventListener("DOMContentLoaded", () => {
    const gridDiv = document.querySelector("#myGrid");
    gridApi = agGrid.createGrid(gridDiv, gridOptions);
});

function showGrid() {
    document.getElementById("gridPlaceholder").style.display = "none";
    document.getElementById("myGrid").style.display = "";
}

function createNewCurriculum() {
    currentSelectedMasterId = null;
    document.getElementById("curriculumTitle").value = "";
    document.getElementById("curriculumTitle").disabled = false;
    document.getElementById("curriculumTitle").focus();

    showGrid();
    setControlState(true, false);
    setEditStatus(true, "새 커리큘럼 작성 중");

    document.querySelectorAll(".curriculum-list-item").forEach((el) =>
        el.classList.remove("bg-sky-50", "border-sky-300", "!border-sky-300"),
    );

    gridApi.setGridOption("rowData", [
        { weekInfo: "1주차", topic: "샘플 주제 입력", content: "샘플 내용 입력" },
    ]);
}

function loadCurriculumDetail(masterId, title) {
    currentSelectedMasterId = masterId;
    document.getElementById("curriculumTitle").value = title;
    document.getElementById("curriculumTitle").disabled = false;

    showGrid();
    setControlState(true, true);
    setEditStatus(true, `"${title}" 편집 중`);

    document.querySelectorAll(".curriculum-list-item").forEach((el) => {
        el.classList.remove("bg-sky-50", "border-sky-300");
    });
    const activeItem = document.getElementById("master-item-" + masterId);
    if (activeItem) activeItem.classList.add("bg-sky-50", "border-sky-300");

    fetch(`/instructor/curriculum/detail/${masterId}`)
        .then((res) => res.json())
        .then((data) => {
            gridApi.setGridOption("rowData", data);
        })
        .catch(() => alert("데이터 로드 실패: 권한이 없거나 에러가 발생했습니다."));
}

function onAddRow() {
    const newItem = { weekInfo: "새 주차", topic: "", content: "" };
    gridApi.applyTransaction({ add: [newItem] });
}

function onRemoveSelectedRow() {
    const selectedRows = gridApi.getSelectedRows();
    if (selectedRows.length === 0) {
        alert("삭제할 행을 표에서 선택해주세요.");
        return;
    }
    gridApi.applyTransaction({ remove: selectedRows });
}

function saveCurriculumData() {
    const title = document.getElementById("curriculumTitle").value.trim();
    if (!title) {
        alert("커리큘럼 제목을 입력해 주세요.");
        return;
    }

    const rowData = [];
    gridApi.forEachNode((node) => rowData.push(node.data));

    if (rowData.length === 0) {
        alert("표에 최소 한 개 이상의 행을 작성해야 합니다.");
        return;
    }

    const url = currentSelectedMasterId
        ? `/instructor/curriculum/modify/${currentSelectedMasterId}`
        : "/instructor/curriculum/save";
    const method = currentSelectedMasterId ? "PUT" : "POST";

    fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: title, detailList: rowData }),
    })
        .then((res) => {
            if (res.ok) {
                alert("성공적으로 저장되었습니다.");
                location.reload();
            } else {
                throw new Error();
            }
        })
        .catch(() => alert("저장 실패: 권한 예외가 발생했습니다."));
}

function deleteCurriculum() {
    if (!currentSelectedMasterId) return;
    if (confirm("정말로 삭제하시겠습니까? (삭제된 데이터는 복구되지 않습니다.)")) {
        fetch(`/instructor/curriculum/delete/${currentSelectedMasterId}`, {
            method: "DELETE",
        })
            .then((res) => {
                if (res.ok) {
                    alert("정상적으로 삭제 처리되었습니다.");
                    location.reload();
                } else {
                    throw new Error();
                }
            })
            .catch(() => alert("삭제 권한이 없거나 처리 오류가 발생했습니다."));
    }
}

function setControlState(activeGrid, showDelete) {
    document.getElementById("btnDetailAction").disabled = !activeGrid;
    document.getElementById("btnDetailActionDel").disabled = !activeGrid;

    document.getElementById("btnSaveMain").style.display = "";
    document.getElementById("btnDeleteMain").style.display = showDelete ? "" : "none";
}

function setEditStatus(show, text) {
    const badge = document.getElementById("editStatusBadge");
    badge.style.display = show ? "flex" : "none";
    if (show) document.getElementById("editStatusText").textContent = text;
}
