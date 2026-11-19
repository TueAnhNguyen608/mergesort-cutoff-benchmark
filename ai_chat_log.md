# Phụ lục: Nhật ký trao đổi với AI hỗ trợ thực hiện thí nghiệm

- **Sinh viên:** Nguyễn Thị Tuệ Anh – MSSV: 25023162
- **Chủ đề:** Thiết kế benchmark, phân tích thống kê và rà soát báo cáo thực nghiệm CUTOFF cho Mergesort
- **Mục đích sử dụng AI:** Tham vấn phương pháp đo hiệu năng chuẩn trong Java (tránh nhiễu JIT/GC), chọn kiểm định thống kê phù hợp cho dữ liệu thời gian chạy và phản biện các lập luận trong báo cáo. Toàn bộ mã nguồn thuật toán (`MergeSort.java`), bộ test (`Test.java`) và việc trực tiếp chạy đo đạc trên máy đều do sinh viên tự thực hiện.

---

## Phiên 1: Thiết kế kịch bản đo hiệu năng (Benchmark) trong Java

### 🧑‍🎓 Sinh viên:
Mình đang làm bài thực nghiệm tìm giá trị `CUTOFF` tối ưu cho thuật toán Mergesort (khi mảng con có độ dài $\le \text{CUTOFF}$ thì chuyển sang Insertion sort) trên Java. Mình đã cài đặt xong `MergeSort.java` theo đúng bài giảng (dùng mảng phụ `aux` cấp phát 1 lần, Insertion sort đổi chỗ liền kề, không bật các tối ưu khác như kiểm tra `a[mid] <= a[mid+1]` để cô lập biến số). 

Giờ mình cần viết class `Benchmark.java` để đo thời gian chạy bằng `System.nanoTime()`. Tuy nhiên, khi chạy thử vài lần đầu thì thấy kết quả nhảy khá loạn, lần đầu tiên bao giờ cũng chậm hơn hẳn các lần sau. Mình nên thiết kế vòng lặp đo như thế nào để hạn chế tối đa nhiễu từ môi trường Java?

### 🤖 AI:
Hiện tượng lần chạy đầu tiên chậm bất thường trong Java là do **JIT Compiler (Just-In-Time)** chưa biên dịch bytecode sang mã máy tối ưu, cùng với việc cấp phát bộ nhớ heap ban đầu. Để kết quả benchmark có độ tin cậy cao, bạn nên áp dụng các kỹ thuật chuẩn sau:

1. **Giai đoạn làm nóng (Warm-up):**
   Trước khi bắt đầu ghi nhận thời gian thật, hãy chạy thuật toán qua một số vòng lặp nháp (ví dụ 10–20 vòng) trên dữ liệu giả và bỏ qua kết quả này. Điều này ép JVM hoàn tất quá trình tối ưu hóa JIT cho các hàm `sort`, `merge` và `insertionSort`.
2. **Cố định kích thước Heap (JVM Flags):**
   Truyền tham số `-Xms2g -Xmx2g` khi chạy chương trình. Việc đặt kích thước heap khởi tạo bằng với kích thước tối đa giúp JVM không phải xin thêm bộ nhớ từ hệ điều hành giữa chừng, giảm thiểu các đợt dừng đột ngột do Garbage Collector (GC).
3. **Đảm bảo tính công bằng giữa các mức CUTOFF (Paired design):**
   Trong mỗi lượt đo (trial) thứ $t$, hãy sinh một mảng gốc từ cùng một hạt giống ngẫu nhiên (`seed = t`), sau đó dùng `.clone()` để tạo bản sao cho từng giá trị `CUTOFF` sắp xếp. Việc `.clone()` phải nằm **ngoài** khối lệnh `System.nanoTime()`.
4. **Xáo trộn thứ tự chạy:**
   Đừng chạy cố định từ `CUTOFF = 1` đến `300` ở mọi lượt, vì nếu CPU bị nóng lên (thermal throttling) ở cuối lượt thì các giá trị `CUTOFF` lớn sẽ luôn chịu thiệt. Hãy dùng `Collections.shuffle()` để xáo trộn danh sách các `CUTOFF` trong mỗi lượt đo.

---

## Phiên 2: Lựa chọn dải giá trị CUTOFF và các kịch bản dữ liệu

### 🧑‍🎓 Sinh viên:
Cảm ơn bạn, mình đã áp dụng warm-up 10 vòng, cố định heap 2GB và xáo trộn thứ tự chạy. Mình định chia 4 loại dữ liệu đầu vào:
1. `random`: ngẫu nhiên hoàn toàn.
2. `duplicates`: nhiều phần tử trùng lặp (giá trị từ `0..99`).
3. `reversed`: mảng giảm dần hoàn toàn.
4. `nearly_sorted`: mảng tăng dần `0..n-1` rồi tráo đổi ngẫu nhiên $n/100$ cặp phần tử.

Về lưới giá trị `CUTOFF`, mình định chọn dày ở đoạn đầu (từ 1 đến 12 từng đơn vị một vì giáo trình nhắc đến 7 và 10), sau đó giãn dần ra: `14, 16, 18, 20, 24, 28, 32, 40, 50, 75, 100, 150, 200, 300`. Thiết kế lưới như vậy có hợp lý không?

### 🤖 AI:
Thiết kế lưới giá trị và 4 tập dữ liệu của bạn rất bài bản và có chủ đích rõ ràng:
* **Lưới phi tuyến tính (dày ở nhỏ, thưa ở lớn):** Rất hợp lý. Vùng từ 1 đến 20 là nơi độ dốc của đường cong thời gian thay đổi nhanh nhất (giảm mạnh chi phí gọi đệ quy), nên việc lấy mẫu từng đơn vị từ 1–12 giúp bạn quan sát chính xác sự chuyển biến quanh mốc 7 và 10 của giáo trình. Việc mở rộng tới 300 cũng rất tốt để quan sát điểm gãy khi chi phí $O(k^2)$ của Insertion sort bắt đầu lấn át.
* **4 loại dữ liệu:** Bao phủ được cả trường hợp trung bình (`random`), trường hợp thực tế nhiều khóa trùng (`duplicates`), trường hợp xấu nhất của Insertion sort (`reversed`) và trường hợp tốt nhất của Insertion sort (`nearly_sorted`).

**Một lưu ý nhỏ:** Với kích thước $n = 10\,000$, thời gian chạy sẽ rất ngắn (chỉ vài trăm micro-giây). Bạn nên chạy lặp lại đủ nhiều lượt (ví dụ 30 trials cho mỗi cấu hình) để đảm bảo mẫu đủ lớn cho bước thống kê sau này.

---

## Phiên 3: Phương pháp thống kê và kiểm định giả thuyết (Python)

### 🧑‍🎓 Sinh viên:
Mình đã chạy xong toàn bộ 9.360 phép đo (4 loại $\times$ 3 cỡ $n$ $\times$ 30 lượt $\times$ 26 CUTOFF) và xuất ra file `results.csv`. Mình đang viết script Python (`analyze.py`) để tổng hợp số liệu. 

Mình nhận thấy trong 30 lượt đo của cùng một cấu hình, thỉnh thoảng vẫn có 1–2 lượt bị vọt lên cao bất thường (chắc do tiến trình nền của Windows xen vào). Nếu dùng giá trị trung bình (mean) thì các điểm nhiễu này kéo kết quả lệch đi khá nhiều. Mình nên dùng chỉ số nào thay thế, và làm sao để chứng minh bằng thống kê xem `CUTOFF = 10` có thực sự nhanh hơn `CUTOFF = 7` hay không?

### 🤖 AI:
Vấn đề bạn gặp phải rất phổ biến khi đo hiệu năng trên hệ điều hành đa nhiệm (như Windows):

1. **Dùng Trung vị (Median) làm chỉ số chính:**
   Thời gian thực thi phần mềm thường có phân phối lệch phải (right-skewed) do các đợt nhiễu từ OS hoặc GC. **Trung vị (Median)** kháng nhiễu (outlier) tốt hơn hẳn so với trung bình cộng (Mean), phản ánh đúng hiệu năng thực tế của thuật toán. Bạn vẫn có thể tính Mean và độ lệch chuẩn (Std) để tham khảo độ ổn định, nhưng nên lấy Median để xếp hạng.
2. **Định nghĩa "Vùng tối ưu" (Sweet spot):**
   Vì đường cong quanh đáy thường khá phẳng và luôn tồn tại sai số đo lường, việc chỉ ra một con số duy nhất (ví dụ 24) là tối ưu tuyệt đối thường thiếu tính thuyết phục. Bạn có thể định nghĩa "vùng tốt" là tập hợp các giá trị `CUTOFF` có thời gian trung vị sai khác không quá 5% so với giá trị nhỏ nhất.
3. **Kiểm định Wilcoxon ghép cặp (Wilcoxon signed-rank test):**
   Để so sánh `CUTOFF = 7` và `CUTOFF = 10`, bạn không nên dùng kiểm định t-test thông thường vì dữ liệu thời gian không tuân theo phân phối chuẩn. Vì ở mỗi lượt đo $t$, cả `CUTOFF = 7` và `CUTOFF = 10` đều sắp xếp **cùng một mảng đầu vào**, đây là dữ liệu ghép cặp (paired samples). Hãy dùng `scipy.stats.wilcoxon` trên 30 cặp giá trị này; nếu $p < 0.05$, bạn có cơ sở thống kê để kết luận sự chênh lệch là có ý nghĩa chứ không phải do ngẫu nhiên.

---

## Phiên 4: Giải thích hiện tượng dị biệt và rà soát lập luận báo cáo

### 🧑‍🎓 Sinh viên:
Sau khi chạy phân tích, mình thấy kết quả của `random` và `duplicates` rất khớp với giả thuyết hình chữ U (đáy nằm quanh vùng 20–40). Tuy nhiên, có hai kết quả hơi lạ mà mình muốn kiểm tra lại cách giải thích của bản thân trước khi viết vào báo cáo:
1. Ở dữ liệu `reversed` (giảm dần), đáy lại tụt về vùng rất nhỏ (6–16), và tới `CUTOFF = 300` thì chậm gấp 6 lần đáy. Mình giải thích là do mảng giảm dần là trường hợp tệ nhất của Insertion sort, mỗi phần tử mới phải dịch chuyển qua toàn bộ các phần tử đứng trước nên chi phí $O(k^2)$ bùng nổ rất sớm.
2. Ở dữ liệu `nearly_sorted`, đáy lại chạy tuốt lên `200–300` (chạm biên trên của lưới đo). Mình nghĩ là do Insertion sort chạy gần như tuyến tính $O(k)$ trên dữ liệu gần có thứ tự, trong khi hàm `merge` của mình không có bước kiểm tra `a[mid] <= a[mid+1]` nên vẫn phải chép mảng và trộn thủ công ở mọi tầng đệ quy. Thành ra `CUTOFF` càng lớn thì Insertion sort càng gánh bớt việc nặng cho `merge`.

Hai lập luận này của mình về mặt độ phức tạp thuật toán có chính xác và chặt chẽ không?

### 🤖 AI:
Cả hai giải thích của bạn đều hoàn toàn chính xác về mặt lý thuyết thuật toán và gắn kết chặt chẽ với mã nguồn bạn đã cài đặt:

* **Với `reversed`:** Khi mảng giảm dần, số phép so sánh và đổi chỗ của Insertion sort đạt cực đại $\frac{k(k-1)}{2}$ trên mỗi mảng con độ dài $k$. Chi phí hằng số của vòng lặp đổi chỗ liền kề lúc này vượt qua chi phí gọi đệ quy và trộn của Mergesort ngay từ khi $k$ còn nhỏ (khoảng 10–16).
* **Với `nearly_sorted`:** Đây là một phát hiện thực nghiệm rất hay. Vì bạn chủ động **không** cài đặt cải tiến kiểm tra `if (a[mid] <= a[mid+1]) return;` để giữ biến số độc lập, hàm `merge` của bạn luôn mất chi phí tuyến tính $\Theta(k)$ để sao chép sang `aux` và trộn ngược lại kể cả khi đoạn đó đã có thứ tự. Trong khi đó, Insertion sort trên mảng gần sắp xếp chỉ tốn chi phí xấp xỉ $O(k)$ tại chỗ mà không tốn chi phí chép mảng phụ. Do đó, tăng `CUTOFF` giúp triệt tiêu các tầng đệ quy `merge` dư thừa.

**Gợi ý khi trình bày trong báo cáo:** Bạn nên nhấn mạnh ở phần *Phân tích* và *Hạn chế* rằng kết quả ở `nearly_sorted` (đáy chạm biên 300) là hệ quả trực tiếp của việc không bật cờ kiểm tra đã sắp xếp, giúp người đọc hiểu rõ phạm vi áp dụng của kết luận.