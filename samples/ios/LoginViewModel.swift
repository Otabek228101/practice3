import SwiftUI
import Shared

final class LoginViewModel: ObservableObject {
    private let validator = LoginValidator()

    @Published var email = "" { didSet { validate() } }
    @Published var password = "" { didSet { validate() } }
    @Published private(set) var emailError: String?
    @Published private(set) var passwordError: String?
    @Published private(set) var canSubmit = false

    private func validate() {
        let result = validator.validate(email: email, password: password)
        emailError = result.emailError
        passwordError = result.passwordError
        canSubmit = result.isValid
    }
}

struct LoginView: View {
    @StateObject private var viewModel = LoginViewModel()

    var body: some View {
        Form {
            TextField("Email", text: $viewModel.email)
            if let error = viewModel.emailError {
                Text(error).foregroundColor(.red)
            }
            SecureField("Password", text: $viewModel.password)
            if let error = viewModel.passwordError {
                Text(error).foregroundColor(.red)
            }
            Button("Log in") {}
                .disabled(!viewModel.canSubmit)
        }
    }
}
