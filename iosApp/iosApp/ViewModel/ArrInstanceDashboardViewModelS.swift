//
//  ArrInstanceDashboardViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-20.
//

import Shared
import SwiftUI

@MainActor
class ArrInstanceDashboardViewModelS: ObservableObject {
    private let viewModel: ArrInstanceDashboardViewModel
    
    @Published private(set) var state: ArrDashboardState = ArrDashboardStateInitial()
    @Published private(set) var isRefreshing: Bool = false
    @Published private(set) var instance: Instance? = nil
    
    init(_ id: Int64) {
        self.viewModel = KoinBridge.shared.getArrInstanceDashboardViewModel(instanceId: id)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.state.observeAsync { self.state = $0 }
        viewModel.isRefreshing.observeAsync { self.isRefreshing = $0.boolValue }
        viewModel.instance.observeAsync { self.instance = $0 }
    }
    
    func refresh() {
        viewModel.refresh()
    }
    
}
