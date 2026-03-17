export type BrandConfig = {
  appName: string;
  supportEmail: string;
  heroMessage: string;
  theme: {
    bg0: string;
    bg1: string;
    neonA: string;
    neonB: string;
    neonC: string;
    dangerA: string;
    dangerB: string;
    text0: string;
    text1: string;
    card: string;
    border: string;
  };
  logoPath: string;
};

export const brand: BrandConfig = {
  appName: "Cric Scorer",
  supportEmail: "surohinitechnologies@gmail.com",
  heroMessage:
    "Welcome to the new world of cricket scoring and player management. Enjoy your app. If you have any queries, please write to surohinitechnologies@gmail.com.",
  theme: {
    // Pitch + Neon
    bg0: "#051014",
    bg1: "#062017",
    neonA: "#7CFFB2",
    neonB: "#00C2FF",
    neonC: "#E7FF3A",
    dangerA: "#FF3B3B",
    dangerB: "#FF006A",
    text0: "#EAF7FF",
    text1: "#A7C4D6",
    card: "rgba(10, 24, 28, 0.72)",
    border: "rgba(124, 255, 178, 0.22)"
  },
  // Put your real company logo at frontend/public and update this path.
  logoPath: "/brand_logo.jpg"
};