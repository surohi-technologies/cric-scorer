import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import ProfilePage from "./pages/ProfilePage";
import DashboardPage from "./pages/DashboardPage";
import { getSession } from "./lib/session";
import { useIdleLogout } from "./lib/useIdleLogout";

function SessionGuard({ children }: { children: React.ReactNode }) {
  useIdleLogout();
  return <>{children}</>;
}

function RequireSession({ children }: { children: React.ReactNode }) {
  const s = getSession();
  if (!s) return <Navigate to="/login" replace />;
  return <SessionGuard>{children}</SessionGuard>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route
        path="/profile"
        element={
          <RequireSession>
            <ProfilePage />
          </RequireSession>
        }
      />
      <Route
        path="/dashboard"
        element={
          <RequireSession>
            <DashboardPage />
          </RequireSession>
        }
      />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}