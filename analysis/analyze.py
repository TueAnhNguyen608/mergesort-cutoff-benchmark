import pandas as pd, numpy as np, matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from scipy import stats

df = pd.read_csv("data/results.csv")
df["ms"] = df.time_ns / 1e6

# 1. Bảng thống kê đầy đủ
tab = df.groupby(["type", "n", "cutoff"]).ms.agg(
    mean="mean", median="median", std="std", count="count").reset_index()
tab["ci95"] = 1.96 * tab["std"] / np.sqrt(tab["count"])
tab.to_csv("data/summary.csv", index=False)

# 2. Đồ thị: thời gian trung vị tương đối so với cutoff=1
for typ, d in tab.groupby("type"):
    plt.figure(figsize=(8, 5))
    for n, dn in d.groupby("n"):
        base = dn.loc[dn.cutoff == 1, "median"].values[0]
        plt.plot(dn.cutoff, dn["median"] / base, marker="o", ms=3, label=f"n={n:,}")
    plt.axvline(7, ls="--", c="gray", label="7 (cũ)")
    plt.axvline(10, ls=":", c="gray", label="10 (mới)")
    plt.xscale("log")
    plt.xlabel("CUTOFF")
    plt.ylabel("Thời gian trung vị tương đối (so với CUTOFF=1)")
    plt.title(f"Dữ liệu: {typ}")
    plt.legend()
    plt.grid(alpha=.3)
    plt.savefig(f"report/fig_{typ}.png", dpi=150)
    plt.close()

# 3. Cutoff tối ưu và vùng tốt (trung vị trong vòng 5% so với tốt nhất)
rows = []
for (typ, n), d in tab.groupby(["type", "n"]):
    best = d.loc[d["median"].idxmin()]
    base = d.loc[d.cutoff == 1, "median"].values[0]
    good = d[d["median"] <= best["median"] * 1.05].cutoff
    rows.append(dict(type=typ, n=n, best_cutoff=int(best.cutoff),
                     speedup_vs_1=round(base / best["median"], 2),
                     good_from=int(good.min()), good_to=int(good.max())))
best_tab = pd.DataFrame(rows)
best_tab.to_csv("data/best_cutoff.csv", index=False)
print("=== CUTOFF tối ưu ===")
print(best_tab.to_string(index=False))

# 4. So sánh cặp CUTOFF 7 vs 10 (cùng dữ liệu mỗi trial)
rows = []
for (typ, n), d in df.groupby(["type", "n"]):
    p = d.pivot(index="trial", columns="cutoff", values="ms")
    rows.append(dict(type=typ, n=n,
                     median_7=round(p[7].median(), 3),
                     median_10=round(p[10].median(), 3),
                     diff_pct=round(100 * (p[7] - p[10]).median() / p[10].median(), 2),
                     p_wilcoxon=round(stats.wilcoxon(p[7], p[10]).pvalue, 4)))
cmp_tab = pd.DataFrame(rows)
cmp_tab.to_csv("data/compare_7_vs_10.csv", index=False)
print("\n=== So sánh 7 vs 10 (diff_pct > 0: 7 chậm hơn 10) ===")
print(cmp_tab.to_string(index=False))